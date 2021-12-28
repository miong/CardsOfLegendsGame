package com.bubul.col.game

import com.bubul.col.game.core.GameManager
import com.bubul.col.game.presenter.GamePresenter
import com.bubul.col.game.ui.UiApp
import com.bubul.col.game.ui.UiAppStateListener

class GameStarter {

    private val game = GameManager()
    private val app = UiApp(object : UiAppStateListener() {
        override fun onExit() {
            game.dispose()
        }
    })
    private lateinit var presenter: GamePresenter

    fun startGame() {
        app.init()
        presenter = GamePresenter(app.ui)
        game.init(presenter)
        app.startUi()
        app.waitReady()
        presenter.init()
        game.setupGamePresenterData()
    }

    fun quitGame() {
        game.dispose()
    }
}