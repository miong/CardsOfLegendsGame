package com.bubul.col.game.ui

import com.badlogic.gdx.Application
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration

open class UiAppStateListener {
    open fun onExit() {}
}

class UiApp(private val listener: UiAppStateListener) {
    private lateinit var app: LwjglApplication
    val ui = UiGame(this)

    fun init() {
        ui.init()
    }

    fun startUi() {
        val config = LwjglApplicationConfiguration().apply {
            title = "CardsOfLegends"
            width = 800
            height = 480
            resizable = false
        }
        app = LwjglApplication(ui, config)
        app.logLevel = Application.LOG_DEBUG
    }

    fun waitReady() {
        while(!ui.uiReady){
            Thread.sleep(1000)
        }
    }

    fun onExit() {
        listener.onExit()
    }
}