package com.bubul.col.game

import com.bubul.col.game.core.GameManager
import com.bubul.col.game.presenter.GamePresenter
import com.bubul.col.game.ui.UiApp
import com.bubul.col.game.ui.UiAppStateListener

class GameStarter {

    private val game = GameManager()
    private val app = UiApp(object : UiAppStateListener() {
        override fun onExit() {
            quitGame()
        }
    })
    private lateinit var presenter: GamePresenter

    fun startGame() {
        app.init()
        presenter = GamePresenter(app.ui)
        val initRes = game.init(presenter)
        app.startUi()
        app.waitReady()
        presenter.init()
        game.setupGamePresenterData()
        if (!initRes) {
            presenter.showInitializationError()
        }
    }

    fun quitGame() {
        try {
            game.dispose()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}