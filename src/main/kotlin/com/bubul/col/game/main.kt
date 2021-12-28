package com.bubul.col.game

fun main() {
    val starter = GameStarter()
    try {
        starter.startGame()
    } catch (e: Exception) {
        e.printStackTrace()
        starter.quitGame()
        throw e
    }

}