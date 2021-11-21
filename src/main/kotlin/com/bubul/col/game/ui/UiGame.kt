package com.bubul.col.game.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.scene2d.Scene2DSkin
import ktx.style.button
import ktx.style.label
import ktx.style.progressBar
import java.nio.file.Paths

class UiGame(private val uiApp: UiApp) : KtxGame<KtxScreen>() {

    var uiReady = false

    fun init()
    {

    }

    override fun create()
    {
        super.create()
        uiReady = true
    }

    override fun dispose() {
        uiApp.onExit()
    }

    fun exit() {
        uiApp.onExit()
        Gdx.app.exit()
    }

}