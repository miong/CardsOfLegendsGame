package com.bubul.col.game.core.net.ping

import com.bubul.col.game.core.net.mqtt.MessageListener
import com.bubul.col.game.core.net.mqtt.MqttClient
import com.bubul.col.messages.ping.PingMsg
import com.bubul.col.messages.ping.PongMsg
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.roundToLong

data class pingValue(val ping : Long, val date : Date)

class PingManager(val entityId : String, val mqttClient: MqttClient) {

    var isRunning  = false
    val mutex = ReentrantLock()
    val pingList = mutableMapOf<String, MutableList<pingValue>>()
    val targets = mutableSetOf<String>()
    lateinit var updaterThread : Thread

    fun start() {
        isRunning = true
        mqttClient.subscribe(PongMsg.topic, object : MessageListener {
            override fun messageArrived(message: ByteArray) {
                val pong = PongMsg.deserialize(message)
                if(pong.target == entityId) {
                    mutex.withLock {
                        val pingValue = (Date().time - pong.time)/2.0
                        if(pingList.size > 10) {
                            pingList[pong.source]?.removeAt(0)
                        }
                        if(!pingList.containsKey(pong.source))
                        {
                            pingList[pong.source] = mutableListOf()
                        }
                        pingList[pong.source]!!.add(pingValue(pingValue.roundToLong(), Date()))
                        if(targets.contains(pong.source))
                        {
                            mqttClient.publish(PingMsg(entityId, pong.source, Date().time))
                        }
                    }
                }
            }
        })
        mqttClient.subscribe(PingMsg.topic, object : MessageListener {
            override fun messageArrived(message: ByteArray) {
                val ping = PingMsg.deserialize(message)
                if(ping.target == entityId) {
                    mqttClient.publish(PongMsg(entityId, ping.source, ping.time))
                }
            }

        })

        updaterThread = Thread(object :Runnable {
            override fun run() {
                while (true) {
                    Thread.sleep(1000)
                    if (!isRunning) {
                        return
                    }
                    mutex.withLock {
                        for (data in pingList.values) {
                            val iter = data.iterator()
                            iter.forEach {
                                if (it.date.before(Date(System.currentTimeMillis() - 5000))) {
                                    iter.remove()
                                }
                            }
                        }
                    }
                }
            }
        })
        updaterThread.start()
    }

    fun addTarget(target : String) {
        mutex.withLock {
            targets.add(target)
            mqttClient.publish(PingMsg(entityId, target, Date().time))
        }
    }

    fun removeTarget(target : String) {
        mutex.withLock {
            targets.remove(target)
        }
    }

    fun getPingFor(target: String) : Long {
        mutex.withLock {
            val list = pingList[target] ?: return -1
            if (list.size < 1)
                return -1
            return list.map { it -> it.ping }.average().roundToLong()
        }
    }

    fun stop() {
        isRunning = false
        mqttClient.unsubscribe(PongMsg.topic)
        mqttClient.unsubscribe(PingMsg.topic)
        updaterThread.join()
    }
}