package com.bubul.col.game.ui

import com.badlogic.gdx.Application
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration

open class UiAppStateListerner  {
    open fun onExit() {}
}

class UiApp(private val listener: UiAppStateListerner) {
    private lateinit var app  : LwjglApplication
    val ui = UiGame(this)

    fun startUi() {
        ui.init()
        val config = LwjglApplicationConfiguration().apply {
            title = "CardsOfLegends"
            width = 800
            height = 480
            resizable = true
        }
        app = LwjglApplication(ui, config)
        app.logLevel = Application.LOG_DEBUG
    }

    fun waitReady() {
        while(!ui.uiReady){
            Thread.sleep(10)
        }
    }

    fun exit() {
        ui.exit()
    }

    fun onExit() {
        listener.onExit()
    }
}