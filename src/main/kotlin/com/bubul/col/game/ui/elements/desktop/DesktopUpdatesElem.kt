package com.bubul.col.game.ui.elements.desktop

import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import ktx.scene2d.KTableWidget
import ktx.scene2d.label
import ktx.scene2d.scene2d
import ktx.scene2d.table

class DesktopUpdatesElem {

    private var container: KTableWidget
    private var titleLabel: Label
    private var textLabel: Label

    init {
        container = scene2d.table {
            titleLabel = label("Cards of legends", "gold-title") {
                setAlignment(Align.top or Align.center)
                it.expandX()
                it.fillX()
                it.height(20f)
                it.padBottom(30f)
            }
            row()
            textLabel = label("", "white-text") {
                setAlignment(Align.top or Align.center)
                it.expand()
                it.fill()
            }
        }
    }


    fun setUpdateTxt(txt: String) {
        textLabel.setText(txt)
    }

    fun getUI(): Table {
        return container
    }
}