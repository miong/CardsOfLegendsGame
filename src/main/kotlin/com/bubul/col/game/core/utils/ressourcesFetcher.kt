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