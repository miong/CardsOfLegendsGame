package com.bubul.col.game.core.net

import com.bubul.col.game.core.net.mqtt.MessageListener
import com.bubul.col.game.core.net.mqtt.MqttClient
import com.bubul.col.game.core.utils.LiveData
import com.bubul.col.messages.ping.PingMsg
import com.bubul.col.messages.ping.PongMsg
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.roundToLong

data class PingValue(val ping: Long, val date: Date)

class PingManager(val entityId: String, val mqttClient: MqttClient) {

    private var isRunning = false
    private val mutex = ReentrantLock()
    private val pingList = mutableMapOf<String, MutableList<PingValue>>()
    private val targets = mutableMapOf<String, LiveData<Long>>()
    private lateinit var updaterThread: Thread

    fun start() {
        isRunning = true
        mqttClient.subscribe(PongMsg.topic, object : MessageListener {
            override fun messageArrived(message: ByteArray) {
                val now = Date().time
                val pong = PongMsg.deserialize(message)
                if (pong.targetEntity == entityId) {
                    mutex.withLock {
                        val pingValue = (now - pong.time) / 2.0
                        if (pingList.containsKey(pong.sourceEntity)) {
                            if (pingList[pong.sourceEntity]!!.size > 10) {
                                pingList[pong.sourceEntity]!!.removeAt(0)
                            }
                        } else {
                            pingList[pong.sourceEntity] = mutableListOf()
                        }
                        pingList[pong.sourceEntity]!!.add(PingValue(pingValue.roundToLong(), Date()))
                        setPingValue(pong.sourceEntity)
                    }
                }
            }
        })
        mqttClient.subscribe(PingMsg.topic, object : MessageListener {
            override fun messageArrived(message: ByteArray) {
                val ping = PingMsg.deserialize(message)
                if (ping.targetEntity == entityId) {
                    mqttClient.publish(PongMsg(entityId, ping.sourceEntity, ping.time))
                }
            }

        })

        updaterThread = Thread {
            while (isRunning) {
                try {
                    mutex.withLock {
                        for (data in pingList) {
                            val iter = data.value.iterator()
                            iter.forEach {
                                if (it.date.before(Date(System.currentTimeMillis() - 60000))) {
                                    iter.remove()
                                }
                            }
                            setPingValue(data.key)
                        }
                        for (target in targets.keys) {
                            mqttClient.publish(PingMsg(entityId, target, Date().time))
                        }
                    }
                    Thread.sleep(30000)
                } catch (e: InterruptedException) {
                    // Nothing to be done
                }
            }
        }
        updaterThread.start()
    }

    private fun setPingValue(source: String) {
        if (pingList[source] == null)
            return
        var ping: Long = -1
        if (pingList[source]!!.size >= 1)
            ping = pingList[source]!!.map { it.ping }.average().roundToLong()
        targets[source]!!.set(ping)
    }

    private fun addTarget(target: String) {
        mutex.withLock {
            targets[target] = LiveData(-1)
            mqttClient.publish(PingMsg(entityId, target, Date().time))
            mqttClient.publish(PingMsg(entityId, target, Date().time))
            mqttClient.publish(PingMsg(entityId, target, Date().time))
            mqttClient.publish(PingMsg(entityId, target, Date().time))
        }
    }

    fun removeTarget(target: String) {
        mutex.withLock {
            targets.remove(target)
        }
    }

    fun getPingFor(target: String): LiveData<Long> {
        mutex.withLock {
            if (!targets.contains(target))
                addTarget(target)
            return targets[target]!!
        }
    }

    fun stop() {
        isRunning = false
        mqttClient.unsubscribe(PongMsg.topic)
        mqttClient.unsubscribe(PingMsg.topic)
        updaterThread.interrupt()
        updaterThread.join()
    }

    fun init() {

    }

    fun dispose() {

    }
}