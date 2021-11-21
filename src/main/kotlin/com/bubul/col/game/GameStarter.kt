package com.bubul.col.game

import com.bubul.col.game.core.GameManager
import com.bubul.col.game.ui.UiApp
import com.bubul.col.game.ui.UiAppStateListerner

class GameStarter {

    private val game = GameManager()
    private val app = UiApp(object : UiAppStateListerner() {
        override fun onExit() {
            game.dispose()
        }
    })

    fun startGame() {
        game.init()
        app.startUi()
        app.waitReady()
    }
}