package com.bubul.col.game.ui.elements.desktop

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.bubul.col.game.core.utils.loadDecomposedGif
import com.bubul.col.game.presenter.DesktopPresenter
import com.bubul.col.game.ui.getGameResource
import ktx.scene2d.*

class DesktopLobbyElem(val presenter: DesktopPresenter) {

    private var container: KTableWidget
    private var gameTypeLabel: Label
    private var adversaryLabel: Label
    private var myLabel: Label
    private var playBtn: Button
    private var closeBtn: Button
    private var inviteTable: Table
    private var invitedAdversaryLabel: Label
    private var invitedAdversaryStatusImage: Image

    init {
        container = scene2d.table {
            gameTypeLabel = label("", "gold-title") {
                it.padBottom(40f)
            }
            row()
            adversaryLabel = label("???????", "gold-title")
            row()
            image(TextureRegionDrawable(TextureRegion(Texture(getGameResource("icons/icons8-player-versus-player-64.png")))))
            row()
            myLabel = label("Player", "gold-title") {
                it.padBottom(40f)
            }
            row()
            inviteTable = table {
                label("Invited : ", "gold-title") {
                    it.padRight(5f)
                }
                invitedAdversaryLabel = label("FriendName", "gold-title") {
                    it.padRight(10f)
                }
                invitedAdversaryStatusImage =
                    image(TextureRegionDrawable(TextureRegion(Texture(getGameResource("icons/icons8-annuler-48.png"))))) {
                        it.size(50f)
                    }
                it.expandX().fillX()
            }
            row()
            playBtn = button {
                label("Play", "gold-title")
                it.width(170f)
                it.height(60f)
            }.apply {
                addListener(object : ClickListener() {
                    override fun clicked(event: InputEvent?, x: Float, y: Float) {
                        if (isDisabled)
                            return
                        presenter.playGame()
                    }
                })
            }
            row()
            closeBtn = button {
                label("Cancel", "gold-title") {
                }
                it.width(170f)
                it.height(60f)
            }.apply {
                addListener(object : ClickListener() {
                    override fun clicked(event: InputEvent?, x: Float, y: Float) {
                        if (isDisabled)
                            return
                        presenter.setUpdateView()
                    }
                })
            }
        }
        inviteTable.isVisible = false
    }

    fun setTypeOfGame(type: String) {
        gameTypeLabel.setText(type)
    }

    fun setAdversaryName(name: String) {
        adversaryLabel.setText(name)
    }

    fun setMyName(type: String) {
        myLabel.setText(type)
    }

    fun getUI(): Table {
        return container
    }

    fun setInvitedAdversary(name: String) {
        invitedAdversaryLabel.setText(name)
        invitedAdversaryStatusImage.drawable =
            loadDecomposedGif("icons/icons8-dots-loading-gif_decomposed", "icons8-dots-loading", 28, 30f)
        inviteTable.addActor(invitedAdversaryStatusImage)
        inviteTable.isVisible = true
    }

    fun invitedAdversaryRefused(name: String) {
        invitedAdversaryLabel.setText(name)
        invitedAdversaryStatusImage.drawable =
            TextureRegionDrawable(TextureRegion(Texture(getGameResource("icons/icons8-annuler-48.png"))))
        invitedAdversaryStatusImage.invalidateHierarchy()
        inviteTable.isVisible = true
    }

    fun invitedAdversaryAccepted(name: String) {
        adversaryLabel.setText(name)
        invitedAdversaryLabel.setText(name)
        invitedAdversaryStatusImage.drawable =
            TextureRegionDrawable(TextureRegion(Texture(getGameResource("icons/icons8-approbation-48.png"))))
        invitedAdversaryStatusImage.invalidateHierarchy()
        inviteTable.isVisible = true
    }

    fun invitedAdversaryLeft(name: String) {
        adversaryLabel.setText(name)
        invitedAdversaryStatusImage.drawable =
            TextureRegionDrawable(TextureRegion(Texture(getGameResource("icons/icons8-annuler-48.png"))))
        invitedAdversaryStatusImage.invalidateHierarchy()
        inviteTable.isVisible = true
    }

    fun resetLobby(defaultName: String) {
        setAdversaryName(defaultName)
        inviteTable.isVisible = false
    }

    fun setCanStartGame(canStartGame: Boolean) {
        playBtn.isDisabled = !canStartGame
    }
}