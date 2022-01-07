package com.bubul.col.game.ui.elements.desktop

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.bubul.col.game.presenter.DesktopPresenter
import ktx.scene2d.*

class DesktopLibraryElem(val presenter: DesktopPresenter) {

    private lateinit var container: Table
    private lateinit var viewContainer: Table
    private lateinit var typeSelector: SelectBox<String>

    init {
        reset()
    }

    fun getUI(): Table {
        return container
    }

    fun reset() {
        container = scene2d.table {
            val pixmap = Pixmap(20, 20, Pixmap.Format.RGBA8888).apply {
                setColor(0f, 0f, 0f, 0.5f)
                fill()
            }
            background = TextureRegionDrawable(TextureRegion(Texture(pixmap)))
            button {
                label("Close", "gold-title")
                it.colspan(2)
                it.expandX().left()
            }.apply {
                addListener(object : ClickListener() {
                    override fun clicked(event: InputEvent?, x: Float, y: Float) {
                        presenter.setUpdateView()
                    }
                })
            }
            row()
            label("Type", "gold-title")
            typeSelector = selectBox<String> {
                -"Heroes"
                -"Spells"
            }.cell(expandX = true, fillX = true).apply {
                addListener(object : ChangeListener() {
                    override fun changed(event: ChangeEvent?, actor: Actor?) {
                        setCardListView(typeSelector.selection.first())
                    }
                })
            }
            row()
            scrollPane { sp ->
                viewContainer = table {

                }
                fadeScrollBars = false
                sp.colspan(2)
                sp.expand().fill()
            }
        }
        setCardListView("Heroes")
    }

    private fun setCardListView(type: String?) {
        Gdx.app.postRunnable {
            type?.let {
                viewContainer.clear()
                var count = 0;
                for (cardView in presenter.getRootCards(it)) {
                    val ui = scene2d.table {
                        add(cardView.getMiniatureImage()).size(50f).align(Align.center)
                        row()
                        add(cardView.getNameLabel().apply {
                            wrap = true
                        }).size(75f, 15f)
                    }.apply {
                        setSize(75f, 70f)
                    }
                    ui.touchable = Touchable.enabled
                    ui.addListener(object : ClickListener() {
                        override fun clicked(event: InputEvent?, x: Float, y: Float) {
                            viewContainer.clear()
                            cardView.resetLibraryView()
                            viewContainer.add(cardView.getLibraryUI()).expand().fill().row()
                            val closeBtn = scene2d.button {
                                label("Return", "gold-title").apply {
                                    setScale(0.5f)
                                }
                            }.apply {
                                setSize(30f, 20f)
                                addListener(object : ClickListener() {
                                    override fun clicked(event: InputEvent?, x: Float, y: Float) {
                                        setCardListView(type)
                                    }
                                })
                            }
                            viewContainer.add(closeBtn).expandX().center()
                        }
                    })
                    viewContainer.add(ui).size(75f, 70f).pad(5f)
                    viewContainer.align(Align.top)
                    count++
                    if (count == 4) {
                        viewContainer.row()
                        count = 0
                    }
                }
            }
        }
    }
}