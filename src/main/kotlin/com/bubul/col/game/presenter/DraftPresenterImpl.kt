package com.bubul.col.game.presenter

import com.bubul.col.game.ui.screens.DraftScreen

class DraftPresenterImpl(private val gamePresenter: GamePresenter, private val screen: DraftScreen) : DraftPresenter {

    fun init() {
        screen.setPresenter(this)
    }

    fun dispose() {

    }

    override fun draftAborted() {
        gamePresenter.draftAborted()
    }
}