package com.bubul.col.game.core.net.mqtt

import com.bubul.col.messages.MqttMessage
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.slf4j.LoggerFactory

interface MessageListener {
    fun messageArrived(message : ByteArray)
}

class MqttClient(val clientId : String) {

    private val internalClient = MqttClient("tcp://cardsoflegendsupdate.bubul.ovh:1883", clientId, MemoryPersistence())
    private val logger = LoggerFactory.getLogger(this.javaClass.name)

    fun connect() {
        val connectionOption = MqttConnectOptions()
        connectionOption.keepAliveInterval = 30
        connectionOption.isAutomaticReconnect = true
        internalClient.connect(connectionOption)
        internalClient.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable?) {
                logger.info("mqtt connection lost !!!!!!!!")
            }

            override fun messageArrived(topic: String?, message: org.eclipse.paho.client.mqttv3.MqttMessage?) {
                logger.info("Message arrived on $topic")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {

            }

        })
        logger.info("Mqtt connected")
    }

    fun publish(msg: MqttMessage) {
        val sentMsg = org.eclipse.paho.client.mqttv3.MqttMessage(msg.serialize())
        sentMsg.qos = 2
        try {
            internalClient.publish(msg.getMqttTopic(), sentMsg)
        } catch (e: Exception) {
            logger.error(e.toString())
            internalClient.reconnect()
            internalClient.publish(msg.getMqttTopic(), sentMsg)
        }
    }

    fun publish(msg: MqttMessage, target: String) {
        val sentMsg = org.eclipse.paho.client.mqttv3.MqttMessage(msg.serialize())
        sentMsg.qos = 2
        try {
            internalClient.publish(msg.getMqttTopic() + target, sentMsg)
        } catch (e: Exception) {
            logger.error(e.toString())
            internalClient.reconnect()
            internalClient.publish(msg.getMqttTopic() + target, sentMsg)
        }
    }

    fun subscribe(topic: String, listener: MessageListener) {
        logger.info("Mqtt subscribe to $topic")
        internalClient.subscribe(topic, object : IMqttMessageListener {
            override fun messageArrived(receivedTopic: String?, message: org.eclipse.paho.client.mqttv3.MqttMessage?) {
                if (topic == receivedTopic) {
                    listener.messageArrived(message!!.payload)
                }
            }

        })
    }

    fun unsubscribe(topic: String) {
        logger.info("Mqtt unsubscribe to $topic")
        try {
            internalClient.unsubscribe(topic)
        } catch (e: Exception) {
            // Nothing to do
        }
    }

    fun unsubscribeAll() {
        logger.info("Mqtt unsubscribe to all topics")
        try {
            internalClient.unsubscribe("*")
        } catch (e: Exception) {
            // Nothing to do
        }
    }

    fun disconnect() {
        internalClient.disconnect()
        logger.info("Mqtt disconnected")
    }

}