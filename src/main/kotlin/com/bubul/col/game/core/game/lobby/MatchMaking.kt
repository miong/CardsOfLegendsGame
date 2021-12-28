package com.bubul.col.game.core.game.lobby

import com.bubul.col.game.core.net.mqtt.MessageListener
import com.bubul.col.game.core.net.mqtt.MqttClient
import com.bubul.col.messages.lobby.MatchMakingProposalMsg
import com.bubul.col.messages.lobby.MatchMakingResultMsg
import com.bubul.col.messages.lobby.MatchMakingSearchMsg
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

interface MatchMakingListener {
    fun onProposalFound()
    fun onWakeUpResult(adversaryId: String, result: Boolean, keepMatchMaking: Boolean, serverSide: Boolean)
    fun onProposalTimeout()
}

enum class MatchMakingStates {
    NOT_SEARCHING,
    SEARCHING,
    PROPOSING,
    AWAKE_TEST
}

class MatchMaking(
    private val listener: MatchMakingListener,
    private val mqttClient: MqttClient,
    private val entityId: String
) {

    var imAwake = AtomicBoolean(false)
    var iAccepted = AtomicBoolean(false)
    var state = MatchMakingStates.NOT_SEARCHING
    var adversaryAccepted = AtomicBoolean(false)
    var selectedAdversaryId = "";
    var searchTimer = Timer()
    var proposalTimer = Timer()
    private val logger = LoggerFactory.getLogger(this.javaClass.name)
    private val searchingEntities = mutableListOf<String>()

    fun start() {
        logger.info("Start matchmaking")
        state = MatchMakingStates.SEARCHING
        mqttClient.subscribe(MatchMakingSearchMsg.topic, object : MessageListener {
            override fun messageArrived(message: ByteArray) {
                val msg = MatchMakingSearchMsg.deserialize(message)
                if (msg.sourceEntity == entityId)
                    return
                logger.info("Received matchmaking search from ${msg.sourceEntity}")
                searchingEntities.add(msg.sourceEntity)
            }
        })
        mqttClient.subscribe(MatchMakingProposalMsg.topic + "/" + entityId, object : MessageListener {
            override fun messageArrived(message: ByteArray) {
                val msg = MatchMakingProposalMsg.deserialize(message)
                if (msg.targetEntity != entityId)
                    return
                logger.info("Received matchmaking proposal from ${msg.sourceEntity}")
                if (state != MatchMakingStates.SEARCHING && state != MatchMakingStates.PROPOSING) {
                    logger.info("$state state make it enqueued")
                    searchingEntities.add(msg.sourceEntity)
                    return
                }
                var serverSide = false
                if (state == MatchMakingStates.SEARCHING) {
                    logger.info("It's the adversary who invite first")
                    val proposalMsg = MatchMakingProposalMsg(entityId, msg.sourceEntity);
                    mqttClient.publish(proposalMsg, "/${msg.sourceEntity}")
                    selectedAdversaryId = msg.sourceEntity
                } else {
                    logger.info("It's me who invite first")
                    serverSide = true
                }
                logger.info("Perform awake test")
                state = MatchMakingStates.AWAKE_TEST
                adversaryAccepted.set(false)
                imAwake.set(false)
                iAccepted.set(false)
                listener.onProposalFound()
                proposalTimer = Timer()
                proposalTimer.schedule(object : TimerTask() {
                    override fun run() {
                        if (!imAwake.get()) {
                            logger.info("Awake test failed")
                            listener.onProposalTimeout()
                            state = MatchMakingStates.SEARCHING
                            iAccepted.set(false)
                        }
                        val proposalAcceptance = iAccepted.get() && adversaryAccepted.get()
                        logger.info("Proposal acceptance is $proposalAcceptance")
                        listener.onWakeUpResult(msg.sourceEntity, proposalAcceptance, iAccepted.get(), serverSide)
                        state = if (proposalAcceptance)
                            MatchMakingStates.NOT_SEARCHING
                        else
                            MatchMakingStates.SEARCHING
                    }
                }, 6000)
            }
        })
        mqttClient.subscribe(MatchMakingResultMsg.topic + entityId, object : MessageListener {
            override fun messageArrived(message: ByteArray) {
                val msg = MatchMakingResultMsg.deserialize(message)
                if (msg.targetEntity != entityId)
                    return
                logger.info("set adversary acceptance to ${msg.accepted}")
                adversaryAccepted.set(msg.accepted)
            }
        })
        val searchMsg = MatchMakingSearchMsg(entityId);
        mqttClient.publish(searchMsg)
        searchTimer = Timer()
        searchTimer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if (state == MatchMakingStates.SEARCHING || state == MatchMakingStates.PROPOSING) {
                    state = MatchMakingStates.SEARCHING
                    if (searchingEntities.isEmpty()) {
                        logger.info("No matchmaking search or proposal received yet")
                        return
                    }
                    selectedAdversaryId = searchingEntities.random()
                    searchingEntities.remove(selectedAdversaryId)
                    logger.info("Matchmaking proposal to $selectedAdversaryId")
                    state = MatchMakingStates.PROPOSING
                    val proposalMsg = MatchMakingProposalMsg(entityId, selectedAdversaryId);
                    mqttClient.publish(proposalMsg, "/$selectedAdversaryId")
                }
            }
        }, 10000, 5000)
    }

    fun stop() {
        logger.info("Stop matchmaking")
        state = MatchMakingStates.NOT_SEARCHING
        searchTimer.cancel()
        searchTimer.purge()
        proposalTimer.cancel()
        proposalTimer.purge()
        mqttClient.unsubscribe(MatchMakingSearchMsg.topic)
        mqttClient.unsubscribe(MatchMakingProposalMsg.topic + "/" + entityId)
        mqttClient.unsubscribe(MatchMakingResultMsg.topic + entityId)
        searchingEntities.clear()
    }

    fun setMyAcceptance(accepted: Boolean) {
        logger.info("set my acceptance to $accepted")
        imAwake.set(true)
        iAccepted.set(accepted)
        val resultMsg = MatchMakingResultMsg(entityId, selectedAdversaryId, iAccepted.get());
        mqttClient.publish(resultMsg, selectedAdversaryId)
    }
}