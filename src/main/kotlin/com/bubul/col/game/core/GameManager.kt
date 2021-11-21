package com.bubul.col.game.core

import com.bubul.col.game.core.net.NetManager
import mu.toKLogger
import org.slf4j.LoggerFactory
import java.util.*

class GameManager {

    val SERVER_ID = "COLServer"

    private val logger = LoggerFactory.getLogger(this.javaClass.name)
    val netManager = NetManager(UUID.randomUUID().toString())


    fun init() {
        logger.info("Initializing the game")
        netManager.connect()
        netManager.getPingManager().start()
        netManager.getPingManager().addTarget(SERVER_ID)
        Thread {
            while (true) {
                Thread.sleep(5000)
                logger.info("ping to server is {}", netManager.getPingManager().getPingFor(SERVER_ID))
            }
        }.start()
    }

    fun dispose() {
        logger.info("Disposing the game")
        netManager.getPingManager().removeTarget(SERVER_ID)
        netManager.getPingManager().stop()
        netManager.disconnect()
    }

}