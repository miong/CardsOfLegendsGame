package com.bubul.col.game.ui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.bubul.col.game.presenter.DraftPresenter
import com.bubul.col.game.ui.getGameResource
import ktx.actors.plusAssign
import ktx.actors.stage
import ktx.app.KtxScreen
import ktx.scene2d.button
import ktx.scene2d.label
import ktx.scene2d.scene2d
import ktx.scene2d.table

class DraftScreen : KtxScreen {

    private lateinit var presenter: DraftPresenter
    private lateinit var rootTable: Table
    private lateinit var screenStage: Stage

    fun init() {
        screenStage = stage()
        rootTable = scene2d.table {
            setFillParent(true)
            label("The game is not yet implemented.\nComing soon, or at least the sooner possible.", "gold-title")
            row()
            button {
                label("Return", "gold-title")
            }.apply {
                addListener(object : ClickListener() {
                    override fun clicked(event: InputEvent?, x: Float, y: Float) {
                        presenter.draftAborted()
                    }
                })
            }
        }
        val backgroundTex = Texture(getGameResource("login_back_image"))
        rootTable.background = TextureRegionDrawable(TextureRegion(backgroundTex))
    }

    override fun show() {
        screenStage += rootTable
        Gdx.input.inputProcessor = screenStage
        super.show()
    }

    override fun render(delta: Float) {
        screenStage.draw()
        screenStage.act()
    }

    override fun hide() {
        screenStage.clear()
        super.hide()
    }

    fun setPresenter(aPresenter: DraftPresenter) {
        presenter = aPresenter
    }
}