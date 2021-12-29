package com.bubul.col.game.core.net

import com.bubul.col.game.core.game.lobby.Lobby
import com.bubul.col.game.core.game.lobby.MatchMaking
import com.bubul.col.game.core.game.lobby.MatchMakingListener
import com.bubul.col.game.core.net.mqtt.MessageListener
import com.bubul.col.game.core.net.mqtt.MqttClient
import com.bubul.col.game.core.utils.LiveData
import com.bubul.col.messages.lobby.LobbyInvitationStatus
import com.bubul.col.messages.lobby.LobbyInviteConfirmationMsg
import com.bubul.col.messages.lobby.LobbyInviteMsg
import com.bubul.col.messages.lobby.LobbyInviteResponseMsg
import org.slf4j.LoggerFactory
import java.util.*

interface LobbyListener {
    fun onLobbyInviteSent(name: String)
    fun onLobbyInvite(name: String)
    fun onInviteCancel(name: String)
    fun onInviteRefused(name: String)
    fun onAdversaryArrival(name: String)
    fun onAdversaryDeparture(name: String)
    fun onFullLobby(friendName: String)
    fun onGameFound()
    fun onGameLaunched(adversaryId: String, serverSide: Boolean)
    fun onGameCancelled()
    fun onGameProposalTimeout()
}

class LobbyManager(val entityId: String, val mqttClient: MqttClient, val friendManager: FriendManager) {

    private val lobby = Lobby()
    private val matchMaker = MatchMaking(object : MatchMakingListener {
        override fun onProposalFound() {
            this@LobbyManager.onProposalFound()
        }

        override fun onWakeUpResult(
            adversaryId: String,
            result: Boolean,
            keepMatchMaking: Boolean,
            serverSide: Boolean
        ) {
            this@LobbyManager.onWakeUpResult(adversaryId, result, keepMatchMaking, serverSide)
        }

        override fun onProposalTimeout() {
            this@LobbyManager.onProposalTimeout()
        }

    }, mqttClient, entityId)

    private var listener: LobbyListener? = null
    private var invitedFriendId: String? = null
    private val canInvite = LiveData(true)
    private val canStartGame = LiveData(true)
    private val invitesQueue: Queue<String> = LinkedList()
    private val logger = LoggerFactory.getLogger(this.javaClass.name)

    fun init() {

    }

    fun connect() {
        mqttClient.subscribe(LobbyInviteMsg.baseTopic + entityId, object : MessageListener {
            override fun messageArrived(message: ByteArray) {
                val msg = LobbyInviteMsg.deserialize(message)
                if (msg.targetEntity != entityId)
                    return
                val friendName = friendManager.getLoginOf(msg.sourceEntity) ?: return
                invitesQueue.add(msg.sourceEntity)
                listener?.onLobbyInvite(friendName)
                friendManager.notifyOnLogout(msg.sourceEntity) {
                    invitesQueue.remove(msg.sourceEntity)
                    listener?.onInviteCancel(friendName)
                }
                logger.info("Game invite received from $friendName")
            }

        })
        mqttClient.subscribe(LobbyInviteResponseMsg.baseTopic + entityId, object : MessageListener {
            override fun messageArrived(message: ByteArray) {
                val msg = LobbyInviteResponseMsg.deserialize(message)
                if (msg.targetEntity != entityId)
                    return
                val friendName = friendManager.getLoginOf(msg.sourceEntity) ?: return
                logger.info("Game invite response received from $friendName")
                if (!msg.accepted && lobby.isOpen()) {
                    if (lobby.getAdversaryId() == msg.sourceEntity) {
                        friendManager.removeNotifyOnLogout(msg.sourceEntity)
                        lobby.unsetAdversary()
                        canStartGame.set(true)
                        canInvite.set(true)
                        listener?.onAdversaryDeparture(lobby.getAdversaryName())
                        logger.info("$friendName leave the lobby")
                    } else {
                        listener?.onInviteRefused(friendName)
                        logger.info("$friendName refuse our invite")
                    }
                    invitedFriendId?.let {
                        friendManager.removeNotifyOnLogout(it)
                    }
                    invitedFriendId = null
                    canInvite.set(true)
                    return
                }
                var status = LobbyInvitationStatus.Accepted
                if (!lobby.isOpen()) {
                    status = LobbyInvitationStatus.Cancelled
                } else if (lobby.isFull()) {
                    status = LobbyInvitationStatus.Full
                }
                val resMsg = LobbyInviteConfirmationMsg(entityId, msg.sourceEntity, status)
                mqttClient.publish(resMsg, msg.sourceEntity)
                if (status == LobbyInvitationStatus.Accepted) {
                    logger.info("$friendName accept our invite")
                    invitedFriendId = null
                    lobby.setAdversary(friendName, msg.sourceEntity)
                    listener?.onAdversaryArrival(friendName)
                    friendManager.notifyOnLogout(msg.sourceEntity) {
                        friendManager.removeNotifyOnLogout(msg.sourceEntity)
                        lobby.unsetAdversary()
                        canStartGame.set(true)
                        canInvite.set(true)
                        listener?.onAdversaryDeparture(lobby.getAdversaryName())
                    }
                }
            }

        })
        mqttClient.subscribe(LobbyInviteConfirmationMsg.baseTopic + entityId, object : MessageListener {
            override fun messageArrived(message: ByteArray) {
                val msg = LobbyInviteConfirmationMsg.deserialize(message)
                if (msg.targetEntity != entityId)
                    return
                val friendName = friendManager.getLoginOf(msg.sourceEntity) ?: return
                logger.info("Game invite confirmation received from $friendName")
                when (msg.status) {
                    LobbyInvitationStatus.Cancelled -> {
                        if (lobby.isOpen() && lobby.getAdversaryId() == msg.sourceEntity) {
                            listener?.onAdversaryDeparture(friendName)
                            lobby.unsetAdversary()
                            canStartGame.set(true)
                            canInvite.set(true)
                        } else {
                            invitesQueue.remove(msg.sourceEntity)
                            listener?.onInviteCancel(friendName)
                        }
                    }
                    LobbyInvitationStatus.Full -> {
                        listener?.onFullLobby(friendName)
                    }
                    LobbyInvitationStatus.Accepted -> {
                        if (!lobby.isOpen()) {
                            lobby.open(false)
                            canStartGame.set(false)
                        }
                        friendManager.notifyOnLogout(msg.sourceEntity) {
                            listener?.onAdversaryDeparture(friendName)
                            lobby.unsetAdversary()
                            canStartGame.set(true)
                            canInvite.set(true)
                        }
                        lobby.setAdversary(friendName, msg.sourceEntity)
                        canInvite.set(false)
                        listener?.onAdversaryArrival(friendName)
                    }
                }
            }

        })
    }

    fun dispose() {
        mqttClient.unsubscribe(LobbyInviteMsg.baseTopic + entityId)
        mqttClient.unsubscribe(LobbyInviteResponseMsg.baseTopic + entityId)
        mqttClient.unsubscribe(LobbyInviteConfirmationMsg.baseTopic + entityId)
    }

    fun setLobbyOpen(open: Boolean, withOwnership: Boolean) {
        logger.info("setLobbyOpen : $open with onwership : $withOwnership")
        if (open) {
            lobby.open(withOwnership)
            canStartGame.set(withOwnership)
        } else {
            lobby.close()
            if (invitedFriendId != null) {
                logger.info("Send Confirmation with Cancelled to $invitedFriendId")
                val msg = LobbyInviteConfirmationMsg(entityId, invitedFriendId!!, LobbyInvitationStatus.Cancelled)
                mqttClient.publish(msg, invitedFriendId!!)
            }
            if (lobby.getAdversaryId() != null) {
                logger.info("Send response with refuse status to ${lobby.getAdversaryId()!!}")
                val msg = LobbyInviteResponseMsg(entityId, lobby.getAdversaryId()!!, false)
                mqttClient.publish(msg, lobby.getAdversaryId()!!)
            }
            invitedFriendId?.let {
                friendManager.removeNotifyOnLogout(it)
            }
            invitedFriendId = null
            lobby.unsetAdversary()
            canStartGame.set(true)
            canInvite.set(true)
        }
    }

    fun canInvite(): LiveData<Boolean> {
        return canInvite
    }

    fun canStartGame(): LiveData<Boolean> {
        return canStartGame
    }

    fun inviteFriend(name: String) {
        if (!lobby.isOpen())
            return
        if (invitedFriendId != null)
            return
        logger.info("Send game invite to $name")
        val friendId = friendManager.getEntityIdOf(name) ?: return
        val msg = LobbyInviteMsg(entityId, friendId)
        mqttClient.publish(msg, friendId)
        invitedFriendId = friendId
        friendManager.notifyOnLogout(invitedFriendId!!) {
            friendManager.getLoginOf(invitedFriendId!!)?.let { listener?.onInviteRefused(it) }
            invitedFriendId = null
            canInvite.set(true)
        }
        canInvite.set(false)
        listener?.onLobbyInviteSent(name)
    }

    fun setListener(aListener: LobbyListener) {
        listener = aListener
    }

    fun disconnect() {

    }

    fun lobbyIsOpen(): Boolean {
        return lobby.isOpen()
    }

    fun isFull(): Boolean {
        return lobby.isFull()
    }

    fun cancelPending() {
        logger.info("Cancel pending invites")
        if (!invitesQueue.isEmpty()) {
            for (friendId in invitesQueue) {
                val msg = LobbyInviteResponseMsg(entityId, friendId, false)
                mqttClient.publish(msg, friendId)
            }
        }
        if (invitedFriendId == null)
            return
        val resMsg = LobbyInviteConfirmationMsg(entityId, invitedFriendId!!, LobbyInvitationStatus.Cancelled)
        mqttClient.publish(resMsg, invitedFriendId!!)
        invitedFriendId = null
        canInvite.set(false)
    }

    fun setTutorialMode() {
        lobby.setTutorialMode()
        canInvite.set(false)
    }

    fun getAdversaryName(): String {
        return lobby.getAdversaryName()
    }

    fun searchAdversary() {
        matchMaker.start()
    }

    fun stopSearchingAdversary() {
        matchMaker.stop()
    }

    fun sendInviteResponse(accepted: Boolean) {
        if (invitesQueue.isEmpty())
            return
        val friendId = invitesQueue.poll()
        logger.info("Send invite response to $friendId")
        val msg = LobbyInviteResponseMsg(entityId, friendId, accepted)
        mqttClient.publish(msg, friendId)
    }

    private fun onWakeUpResult(adversaryId: String, result: Boolean, keepMatchMaking: Boolean, serverSide: Boolean) {
        if (result) {
            listener?.onGameLaunched(adversaryId, serverSide)
        } else {
            if (keepMatchMaking)
                listener?.onGameCancelled()
        }
    }

    private fun onProposalFound() {
        listener?.onGameFound()
    }

    private fun onProposalTimeout() {
        listener?.onGameProposalTimeout()
    }

    fun acceptGameProposal() {
        matchMaker.setMyAcceptance(true)
    }

    fun refuseGameProposal() {
        matchMaker.setMyAcceptance(false)
    }

    fun getAdversaryId(): String {
        lobby.getAdversaryId()?.let {
            return it
        }
        return entityId
    }

    fun isLobbyOwner(): Boolean {
        return lobby.isOwner()
    }


}