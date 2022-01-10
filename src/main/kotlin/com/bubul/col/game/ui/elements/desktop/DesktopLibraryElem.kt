package com.bubul.col.game.ui.elements.desktop

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.bubul.col.game.presenter.DesktopPresenter
import com.bubul.col.game.ui.utils.TextureBuilder
import ktx.scene2d.*

class DesktopLibraryElem(val presenter: DesktopPresenter) {

    private lateinit var container: Table
    private lateinit var viewContainer: Table
    private lateinit var typeSelector: SelectBox<String>

    private val heroesCategory = "Heroes"
    private val spellsCategory = "Spells"

    fun init() {
    }

    fun getUI(): Table {
        return container
    }

    fun reset() {
        container = scene2d.table {
            background = TextureBuilder.getColorFilledTextureRegionDrawable(20, 20, Color(0f, 0f, 0f, 0.5f))
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
                -heroesCategory
                -spellsCategory
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
    }

    private fun setCardListView(type: String?) {
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

    fun loadCardView() {
        //TODO fix lattency plus loading not visible.
        // Need to split loader of views :/
        Thread {
            Gdx.app.postRunnable {
                viewContainer.clear()
                viewContainer.add(scene2d.label("Loading", "gold-title"))
            }
            Thread.sleep(1000)
            Gdx.app.postRunnable {
                //force load
                presenter.getRootCards(heroesCategory)
                setCardListView(heroesCategory)
            }
        }.start()
    }
}