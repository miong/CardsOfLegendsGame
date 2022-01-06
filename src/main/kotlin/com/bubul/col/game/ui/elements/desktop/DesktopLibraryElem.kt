package com.bubul.col.game.ui.elements.desktop

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
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
                -"Passives"
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
                sp.colspan(2)
                sp.expand().fill()
            }
        }
        setCardListView("Heroes")
    }

    private fun setCardListView(first: String?) {
        Gdx.app.postRunnable {
            first?.let {
                viewContainer.clear()
                var count = 0;
                for (cardView in presenter.getRootCards(it)) {
                    val ui = cardView.getMiniature()
                    ui.touchable = Touchable.enabled
                    ui.clearListeners()
                    viewContainer.add(ui).size(50f).pad(5f).top().left()
                    count++
                    if (count == 6) {
                        viewContainer.row()
                        count = 0
                    }
                }
            }
        }
    }
}