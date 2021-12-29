package com.bubul.col.game.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import java.nio.file.Paths

fun getGameResource(resPath: String): FileHandle? {
    var file = Gdx.files.absolute(Paths.get("", resPath).toAbsolutePath().toString())
    if (!file.exists())
        file = Gdx.files.absolute(Paths.get("", "resources/$resPath").toAbsolutePath().toString())
    return file
}

fun getLoggerConfiguration(): String {
    var file = Paths.get("", "log4j2.xml").toAbsolutePath().toFile()
    if (!file.exists())
        file = Paths.get("", "resources/log4j2.xml").toAbsolutePath().toFile()
    return file.absolutePath
}