package com.bubul.col.game.core.net

import com.bubul.col.game.core.net.mqtt.MqttClient

class NetManager(clientId: String) {

    private val mqttClient = MqttClient(clientId)
    private val pingManager = PingManager(clientId, mqttClient)
    private val loginManager = LoginManager(clientId, mqttClient)
    private val friendManager = FriendManager(clientId, mqttClient, loginManager)
    private val chatManager = ChatManager(clientId, mqttClient, friendManager)
    private val lobbyManager = LobbyManager(clientId, mqttClient, friendManager)

    fun init() {
        pingManager.init()
        loginManager.init()
        chatManager.init()
        friendManager.init()
        lobbyManager.init()
    }

    fun dispose() {
        pingManager.dispose()
        loginManager.dispose()
        chatManager.dispose()
        friendManager.dispose()
        lobbyManager.dispose()
        mqttClient.unsubscribeAll()
    }

    fun connect() {
        mqttClient.connect()
        chatManager.connect()
        lobbyManager.connect()
    }

    fun disconnect() {
        chatManager.disconnect()
        lobbyManager.disconnect()
        mqttClient.disconnect()
    }

    fun getPingManager(): PingManager {
        return pingManager
    }

    fun getLoginManager(): LoginManager {
        return loginManager
    }

    fun getChatManager(): ChatManager {
        return chatManager
    }

    fun getFriendManager(): FriendManager {
        return friendManager
    }

    fun getLobbyManager(): LobbyManager {
        return lobbyManager
    }
}