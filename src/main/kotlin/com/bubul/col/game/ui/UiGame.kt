package com.bubul.col.game.ui

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.bubul.col.game.ui.screens.DesktopScreen
import com.bubul.col.game.ui.screens.DraftScreen
import com.bubul.col.game.ui.screens.LoginScreen
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.scene2d.Scene2DSkin


class UiGame(private val uiApp: UiApp) : KtxGame<KtxScreen>() {

    var uiReady = false

    lateinit var loginScreen: LoginScreen
    lateinit var desktopScreen: DesktopScreen
    lateinit var draftScreen: DraftScreen

    fun init() {
        loginScreen = LoginScreen()
        addScreen(loginScreen)
        desktopScreen = DesktopScreen()
        addScreen(desktopScreen)
        draftScreen = DraftScreen()
        addScreen(draftScreen)
    }

    override fun create() {
        val atlas = TextureAtlas(getGameResource("neon_skin/neon-ui.atlas"))
        val neonSkin = Skin(getGameResource("neon_skin/neon-ui.json"), atlas)
        Scene2DSkin.defaultSkin = neonSkin

        loginScreen.init()
        desktopScreen.init()
        draftScreen.init()
        setScreen<LoginScreen>()
        super.create()
        uiReady = true
    }

    override fun dispose() {
        uiApp.onExit()
    }

    fun quit() {
        uiApp.quit()
    }

    fun showDesktop() {
        setScreen<DesktopScreen>()
    }

    fun showDraft() {
        setScreen<DraftScreen>()
    }
}