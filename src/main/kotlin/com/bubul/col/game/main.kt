package com.bubul.col.game

import com.bubul.col.game.ui.getLoggerConfigurationPath
import org.apache.logging.log4j.core.config.ConfigurationSource
import org.apache.logging.log4j.core.config.Configurator
import java.io.FileInputStream

fun main() {
    val source = ConfigurationSource(FileInputStream(getLoggerConfigurationPath()))
    Configurator.initialize(null, source)
    val starter = GameStarter()
    try {
        starter.startGame()
    } catch (e: Exception) {
        e.printStackTrace()
        starter.quitGame()
        throw e
    }

}