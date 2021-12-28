package com.bubul.col.game.ui.elements.desktop

import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.bubul.col.game.presenter.DesktopPresenter
import com.bubul.col.game.presenter.GameType
import ktx.scene2d.*

class DesktopGameTypeSelectorElem(val presenter: DesktopPresenter) {

    private var container: KTableWidget
    private var gameModeBtn: Button
    private var tutoModeBtn: Button
    private var closeBtn: Button

    init {
        container = scene2d.table {
            label("Select game type", "gold-title") {
                it.padBottom(40f)
            }
            row()
            table {
                closeBtn = button {
                    label("Cancel", "gold-title") {
                    }
                    it.width(170f)
                    it.height(60f)
                }.apply {
                    addListener(object : ClickListener() {
                        override fun clicked(event: InputEvent?, x: Float, y: Float) {
                            presenter.setUpdateView()
                        }
                    })
                }
                it.expandX()
                it.align(Align.right or Align.top)
                it.padBottom(40f)
            }
            row()
            gameModeBtn = button {
                label("Normal", "gold-title") {
                }
                it.width(170f)
                it.height(60f)
            }.apply {
                addListener(object : ClickListener() {
                    override fun clicked(event: InputEvent?, x: Float, y: Float) {
                        presenter.setGameType(GameType.Normal)
                        presenter.setLobbyView(true)
                    }
                })
            }
            row()
            tutoModeBtn = button {
                label("Tutorial", "gold-title") {
                }
                it.width(170f)
                it.height(60f)
            }.apply {
                addListener(object : ClickListener() {
                    override fun clicked(event: InputEvent?, x: Float, y: Float) {
                        presenter.setGameType(GameType.Tutorial)
                        presenter.setLobbyView(true)
                    }
                })
            }
        }
    }

    fun getUI(): Table {
        return container
    }
}