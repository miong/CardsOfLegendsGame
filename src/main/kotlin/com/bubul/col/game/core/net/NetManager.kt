package com.bubul.col.game.core.net

import com.bubul.col.game.core.net.mqtt.MqttClient
import com.bubul.col.game.core.net.ping.PingManager

class NetManager(val clientId : String) {

    private val mqttClient = MqttClient(clientId)
    private val pingManager = PingManager(clientId, mqttClient)

    fun connect() {
        mqttClient.connect()
    }

    fun disconnect() {
        mqttClient.disconnect()
    }

    fun getMqttClient() : MqttClient {
        return mqttClient
    }

    fun getPingManager() : PingManager {
        return pingManager
    }
}