package com.bubul.col.game.core.net

import com.bubul.col.game.core.net.mqtt.MessageListener
import com.bubul.col.game.core.net.mqtt.MqttClient
import com.bubul.col.game.ui.getResourcePath
import com.bubul.col.messages.login.*
import org.slf4j.LoggerFactory
import java.io.File
import java.security.MessageDigest
import java.util.*
import javax.xml.bind.DatatypeConverter

enum class LoginStatus {
    Success,
    Failed
}

interface LoginListener {
    fun onLoginResult(result: LoginStatus)
    fun onRegisterResult(result: LoginStatus)
}

class LoginManager(val entityId: String, val mqttClient: MqttClient) {

    private var listener: LoginListener? = null
    var loggedId: String? = null
    private val logger = LoggerFactory.getLogger(this.javaClass.name)

    fun init() {
    }

    fun dispose() {
        mqttClient.unsubscribe(LoginRestultMsg.topic)
        mqttClient.unsubscribe(RegisterRestulMsg.topic)
    }

    fun setListener(aListener: LoginListener) {
        listener = aListener
    }

    fun login(login: String, mdp: String) {
        val salt = hashMdp(mdp)
        val msg = LoginMsg(entityId, login, salt)
        mqttClient.subscribe(LoginRestultMsg.topic, object : MessageListener {
            override fun messageArrived(message: ByteArray) {
                val loginMsg = LoginRestultMsg.deserialize(message)
                if (loginMsg.targetEntity == entityId) {
                    val res =
                        if (loginMsg.resultItem == LoginResultItem.Success) LoginStatus.Success else LoginStatus.Failed
                    if (res == LoginStatus.Success) {
                        loggedId = login
                        mqttClient.unsubscribe(LoginRestultMsg.topic)
                        logger.info("User logged in")
                    } else {
                        logger.error("Login failed")
                    }
                    listener?.onLoginResult(res)
                }
            }
        })
        mqttClient.publish(msg)
    }

    fun register(login: String, mdp: String) {
        val salt = hashMdp(mdp)
        val msg = RegisterMsg(entityId, login, salt)
        mqttClient.subscribe(RegisterRestulMsg.topic, object : MessageListener {
            override fun messageArrived(message: ByteArray) {
                val registerMsg = RegisterRestulMsg.deserialize(message)
                if (registerMsg.targetEntity == entityId) {
                    val res =
                        if (registerMsg.resultItem == LoginResultItem.Success) LoginStatus.Success else LoginStatus.Failed
                    if (res == LoginStatus.Success) {
                        loggedId = login
                        mqttClient.unsubscribe(RegisterRestulMsg.topic)
                        logger.info("User registered and logged in")
                    } else {
                        logger.error("Registering failed")
                    }
                    listener?.onRegisterResult(res)
                }
            }
        })
        mqttClient.publish(msg)
    }

    private fun hashMdp(mdp: String): String {
        val bytes = MessageDigest
            .getInstance("SHA-256")
            .digest(mdp.toByteArray())
        return DatatypeConverter.printHexBinary(bytes).uppercase(Locale.getDefault())
    }

    fun logout(entityId: String) {
        if (loggedId != null) {
            logger.info("Logout")
            val msg = LogoutMsg(entityId, loggedId!!)
            mqttClient.publish(msg)
        }
    }

    fun forceGameUpdate() {
        val versioningFile = File(getResourcePath("dataversioning.db"))
        if (versioningFile.exists())
            versioningFile.delete()
    }
}