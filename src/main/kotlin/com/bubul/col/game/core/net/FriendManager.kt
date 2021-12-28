package com.bubul.col.game.core.net

import com.bubul.col.game.core.net.mqtt.MessageListener
import com.bubul.col.game.core.net.mqtt.MqttClient
import com.bubul.col.messages.friend.*
import com.bubul.col.messages.login.LoginRestultMsg
import com.bubul.col.messages.login.LoginResultItem
import com.bubul.col.messages.login.LogoutMsg
import org.slf4j.LoggerFactory

enum class FriendStatus {
    Offline,
    Online,
    Playing
}

data class FriendDesc(val name: String, var status: FriendStatus, var entityId: String)

interface FriendListener {
    fun onFriendLoaded(friends: List<FriendDesc>)
    fun onFriendConnect(friend: String)
    fun onFriendDisconnect(friend: String)
    fun onFriendRequest(friend: String)
    fun onFriendAdded(friend: FriendDesc)
    fun onFriendRemove(friend: String)
    fun onFriendStatusUpdate(friend: String, status: FriendStatus)
}

class FriendManager(val clientId: String, val mqttClient: MqttClient, val loginManager: LoginManager) {

    private var listener: FriendListener? = null
    private val friendsMap = mutableMapOf<String, FriendDesc>()
    private val logger = LoggerFactory.getLogger(this.javaClass.name)
    private val notifyLogoutMap = mutableMapOf<String, () -> Unit>()

    fun init() {
    }

    fun dispose() {
        mqttClient.unsubscribe(LoginRestultMsg.topic)
        mqttClient.unsubscribe(LogoutMsg.topic)
        mqttClient.unsubscribe(FriendResponseMsg.topic)
        mqttClient.unsubscribe(FriendRemovalMsg.topic)
        mqttClient.unsubscribe(FriendRequestListMsg.topic)
        mqttClient.unsubscribe(FriendStatusListMsg.topic)
        mqttClient.unsubscribe(FriendStatusMsg.topic)
    }

    fun setListener(aListerner: FriendListener) {
        listener = aListerner
    }

    fun isFriend(name: String): Boolean {
        return friendsMap.containsKey(name)
    }

    fun loadFriends() {
        logger.info("Loading friends")
        mqttClient.subscribe(FriendStatusListMsg.topic, object : MessageListener {
            override fun messageArrived(message: ByteArray) {
                val friendStatusListMsg = FriendStatusListMsg.deserialize(message)
                if (friendStatusListMsg.target == loginManager.loggedId!!) {
                    val friendList = mutableListOf<FriendDesc>()
                    for (item in friendStatusListMsg.friends) {
                        var status = when (item.value.first) {
                            com.bubul.col.messages.friend.FriendStatus.Offline -> FriendStatus.Offline
                            com.bubul.col.messages.friend.FriendStatus.Online -> FriendStatus.Online
                            com.bubul.col.messages.friend.FriendStatus.Playing -> FriendStatus.Playing
                        }
                        val desc = FriendDesc(item.key, status, item.value.second)
                        friendList.add(desc)
                        friendsMap[item.key] = desc
                    }
                    listener?.onFriendLoaded(friendList)
                    mqttClient.unsubscribe(FriendStatusListMsg.topic)
                    logger.info("Friends loaded")
                }
            }
        })
        mqttClient.subscribe(FriendRequestListMsg.topic, object : MessageListener {
            override fun messageArrived(message: ByteArray) {
                val friendRequestListMsg = FriendRequestListMsg.deserialize(message)
                if (friendRequestListMsg.targetEntity == clientId) {
                    for (request in friendRequestListMsg.requests) {
                        logger.info("Friends request from $request")
                        listener?.onFriendRequest(request)
                    }
                    mqttClient.unsubscribe(FriendRequestListMsg.topic)
                }
            }
        })
        mqttClient.subscribe(LoginRestultMsg.topic, object : MessageListener {
            override fun messageArrived(message: ByteArray) {
                val loginMsg = LoginRestultMsg.deserialize(message)
                if (isFriend(loginMsg.login) && loginMsg.resultItem == LoginResultItem.Success) {
                    friendsMap[loginMsg.login]?.let {
                        it.entityId = loginMsg.targetEntity
                        listener?.onFriendConnect(loginMsg.login)
                    }
                }
            }
        })
        mqttClient.subscribe(LogoutMsg.topic, object : MessageListener {
            override fun messageArrived(message: ByteArray) {
                val logoutMsg = LogoutMsg.deserialize(message)
                if (isFriend(logoutMsg.login)) {
                    notifyLogoutMap[logoutMsg.sourceEntity]?.invoke()
                    friendsMap[logoutMsg.login]!!.status = FriendStatus.Offline
                    listener?.onFriendDisconnect(logoutMsg.login)
                }
            }
        })
        mqttClient.subscribe(FriendRequestMsg.topic, object : MessageListener {
            override fun messageArrived(message: ByteArray) {
                val friendRequestMsg = FriendRequestMsg.deserialize(message)
                if (friendRequestMsg.target == loginManager.loggedId!!) {
                    listener?.onFriendRequest(friendRequestMsg.source)
                }
            }
        })
        mqttClient.subscribe(FriendResponseMsg.topic, object : MessageListener {
            override fun messageArrived(message: ByteArray) {
                val friendResponseMsg = FriendResponseMsg.deserialize(message)
                if (friendResponseMsg.target == loginManager.loggedId && friendResponseMsg.accepted) {
                    val status =
                        if (friendResponseMsg.sourceEntity.isNotEmpty()) FriendStatus.Online else FriendStatus.Offline
                    val desc = FriendDesc(friendResponseMsg.source, status, friendResponseMsg.sourceEntity)
                    friendsMap[friendResponseMsg.source] = desc
                    listener?.onFriendAdded(desc)
                }
            }
        })
        mqttClient.subscribe(FriendRemovalMsg.topic, object : MessageListener {
            override fun messageArrived(message: ByteArray) {
                val friendRemovalMsg = FriendRemovalMsg.deserialize(message)
                if (isFriend(friendRemovalMsg.source) && friendRemovalMsg.target == loginManager.loggedId) {
                    friendsMap.remove(friendRemovalMsg.source)
                    listener?.onFriendRemove(friendRemovalMsg.source)
                }
            }
        })
        mqttClient.subscribe(FriendStatusMsg.topic, object : MessageListener {
            override fun messageArrived(message: ByteArray) {
                val friendStatusMsg = FriendStatusMsg.deserialize(message)
                if (isFriend(friendStatusMsg.source)) {
                    friendsMap[friendStatusMsg.source]!!.status = when (friendStatusMsg.status) {
                        com.bubul.col.messages.friend.FriendStatus.Offline -> FriendStatus.Offline
                        com.bubul.col.messages.friend.FriendStatus.Online -> FriendStatus.Online
                        com.bubul.col.messages.friend.FriendStatus.Playing -> FriendStatus.Playing
                    }
                    listener?.onFriendStatusUpdate(friendStatusMsg.source, friendsMap[friendStatusMsg.source]!!.status)
                }
            }
        })
        val friendStatusRequestMsg = FriendStatusRequestMsg(loginManager.loggedId!!, clientId)
        mqttClient.publish(friendStatusRequestMsg)
    }

    fun sendFriendRequest(target: String) {
        if (target == loginManager.loggedId!!)
            return
        logger.info("Sends friend request for $target")
        val msg = FriendRequestMsg(loginManager.loggedId!!, target)
        mqttClient.publish(msg)
    }

    fun sendFriendRequestResponse(friendLogin: String, accepted: Boolean) {
        logger.info("Sends friend request result for $friendLogin with accepted value of $accepted")
        val msg = FriendResponseMsg(loginManager.loggedId!!, friendLogin, accepted, clientId)
        mqttClient.publish(msg)
    }

    fun removeFriend(target: String) {
        val msg = FriendRemovalMsg(loginManager.loggedId!!, target)
        mqttClient.publish(msg)
        listener?.onFriendRemove(msg.target)
    }

    fun setPlaying() {
        val msg = FriendStatusMsg(loginManager.loggedId!!, com.bubul.col.messages.friend.FriendStatus.Playing, clientId)
        mqttClient.publish(msg)
    }

    fun setOnline() {
        val msg = FriendStatusMsg(loginManager.loggedId!!, com.bubul.col.messages.friend.FriendStatus.Online, clientId)
        mqttClient.publish(msg)
    }

    fun getEntityIdOf(target: String): String? {
        friendsMap[target]?.let {
            return it.entityId
        }
        return null
    }

    fun getLoginOf(targetId: String): String? {
        for (desc in friendsMap.values) {
            if (desc.entityId == targetId)
                return desc.name
        }
        return null
    }

    fun notifyOnLogout(friendId: String, bloc: () -> Unit) {
        notifyLogoutMap[friendId] = bloc
    }

    fun removeNotifyOnLogout(friendId: String) {
        notifyLogoutMap.remove(friendId)
    }
}