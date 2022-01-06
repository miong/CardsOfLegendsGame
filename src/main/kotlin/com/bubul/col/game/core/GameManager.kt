package com.bubul.col.game.core

import com.bubul.col.game.core.game.cards.CardManager
import com.bubul.col.game.core.net.NetManager
import com.bubul.col.game.presenter.GamePresenter
import org.slf4j.LoggerFactory
import java.util.*

class GameManager {

    private val SERVER_ID = "COLServer"

    private lateinit var gamePresenter: GamePresenter
    private var gameRunning = false
    private val logger = LoggerFactory.getLogger(this.javaClass.name)
    private val entityId = UUID.randomUUID().toString()
    private val netManager = NetManager(entityId)
    private val cardManager = CardManager()


    fun init(aPresenter: GamePresenter): Boolean {
        gamePresenter = aPresenter
        gameRunning = true
        logger.info("Initializing the game")
        netManager.init()
        netManager.connect()
        netManager.getPingManager().start()
        netManager.getLoginManager().setListener(gamePresenter.loginPresenter.getLoginListener())
        netManager.getFriendManager().setListener(gamePresenter.desktopPresenter.getFriendListener())
        netManager.getChatManager().setListener(gamePresenter.desktopPresenter.getChatListener())
        netManager.getLobbyManager().setListener(gamePresenter.desktopPresenter.getLobbyListener())
        if (!cardManager.init()) {
            logger.info("Initialization failed")
            return false
        }
        logger.info("Initialization done")
        return true
    }

    fun setupGamePresenterData() {
        gamePresenter.loginPresenter.setPingToServer(netManager.getPingManager().getPingFor(SERVER_ID))
        gamePresenter.loginPresenter.setLoginManager(netManager.getLoginManager())
        gamePresenter.desktopPresenter.setPingToServer(netManager.getPingManager().getPingFor(SERVER_ID))
        gamePresenter.desktopPresenter.setChatManager(netManager.getChatManager())
        gamePresenter.desktopPresenter.setFriendManager(netManager.getFriendManager())
        gamePresenter.desktopPresenter.setLoginManager(netManager.getLoginManager())
        gamePresenter.desktopPresenter.setLobbyManager(netManager.getLobbyManager())
        gamePresenter.desktopPresenter.setCardManager(cardManager)
    }

    fun dispose() {
        gameRunning = false
        logger.info("Disposing the game")
        netManager.getLoginManager().logout(entityId)
        netManager.getPingManager().removeTarget(SERVER_ID)
        netManager.getPingManager().stop()
        netManager.disconnect()
        netManager.dispose()
        gamePresenter.dispose()
        logger.info("Disposing done")
    }

}