package com.bubul.col.game.ui

import com.badlogic.gdx.Application
import com.badlogic.gdx.Files
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import java.nio.file.Paths
import kotlin.io.path.exists

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
            if (Paths.get("", "icon.png").toAbsolutePath().exists())
                addIcon(Paths.get("", "icon.png").toAbsolutePath().toString(), Files.FileType.Absolute)
            else
                addIcon(Paths.get("", "resources/icon.png").toAbsolutePath().toString(), Files.FileType.Absolute)
        }
        app = LwjglApplication(ui, config)
        app.logLevel = Application.LOG_DEBUG
    }

    fun waitReady() {
        while (!ui.uiReady) {
            Thread.sleep(1000)
        }
    }

    fun onExit() {
        listener.onExit()
    }

    fun quit() {
        app.exit()
    }
}