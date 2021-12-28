package com.bubul.col.game.core.net

import com.bubul.col.game.core.net.mqtt.MessageListener
import com.bubul.col.game.core.net.mqtt.MqttClient
import com.bubul.col.messages.chat.ChatMsg
import org.slf4j.LoggerFactory

interface ChatListener {
    fun setMessages(messages: List<Pair<String, String>>)
    fun onMessage(sender: String, content: String)
}

class ChatManager(private val entityId: String, private val mqttClient: MqttClient, val friendManager: FriendManager) {

    private var listener: ChatListener? = null
    private var messagesMap = mutableMapOf<String, MutableList<Pair<String, String>>>()
    private val logger = LoggerFactory.getLogger(this.javaClass.name)

    fun connect() {
        mqttClient.subscribe(ChatMsg.baseTopic + entityId, object : MessageListener {
            override fun messageArrived(message: ByteArray) {
                val msg = ChatMsg.deserialize(message)
                logger.info("Receive chat message")
                val sourceLogin = friendManager.getLoginOf(msg.source) ?: return
                logger.info("Receive message from $sourceLogin")
                if (messagesMap[sourceLogin] == null)
                    messagesMap[sourceLogin] = mutableListOf()
                messagesMap[sourceLogin]!!.add(Pair(sourceLogin, msg.content))
                listener?.onMessage(sourceLogin, msg.content)
            }
        })
    }

    fun sendMessage(target: String, content: String) {
        logger.info("Send message to $target")
        val targetEntity = friendManager.getEntityIdOf(target) ?: return
        val msg = ChatMsg(entityId, targetEntity, content)
        mqttClient.publish(msg, targetEntity)
        if (messagesMap[target] == null)
            messagesMap[target] = mutableListOf()
        messagesMap[target]!!.add(Pair(entityId, content))
    }

    fun loadMessages(with: String) {
        var messages = messagesMap[with]
        if (messages == null)
            messages = mutableListOf()
        listener?.setMessages(messages)
    }

    fun setListener(aListener: ChatListener) {
        listener = aListener
    }

    fun disconnect() {
        mqttClient.unsubscribe(ChatMsg.baseTopic + entityId)
    }

    fun init() {

    }

    fun dispose() {

    }
}