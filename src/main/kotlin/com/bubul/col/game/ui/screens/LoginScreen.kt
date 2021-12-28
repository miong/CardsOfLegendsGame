package com.bubul.col.game.ui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.bubul.col.game.presenter.LoginPresenter
import com.bubul.col.game.ui.getGameResource
import ktx.actors.plusAssign
import ktx.actors.stage
import ktx.app.KtxScreen
import ktx.scene2d.*
import org.slf4j.LoggerFactory

class LoginScreen : KtxScreen {

    private lateinit var stage: Stage
    private lateinit var rootTable: Table
    lateinit var loginBt: Button
    lateinit var registerBt: Button
    lateinit var loginText: TextField
    lateinit var pswdText: TextField
    lateinit var pingLabel: Label
    private lateinit var errorMessageDialog: Dialog

    lateinit var presenter: LoginPresenter

    private val logger = LoggerFactory.getLogger(this.javaClass.name)

    fun init() {
        stage = stage()
        rootTable = scene2d.table {
            setFillParent(true)
            label("Ping : ", "gold-title") {
                it.padTop(10f)
                it.right()
                it.expand()
            }.apply {
                this.fontScaleY = 0.6f
                this.fontScaleX = 0.6f
            }
            pingLabel = label("inf", "gold-title") {
                it.padTop(10f)
                it.right()
                it.padRight(10f)
            }.apply {
                this.fontScaleY = 0.6f
                this.fontScaleX = 0.6f
            }
            row()
            label("Cards of Legends", "gold-title") {
                it.expand(false, true)
                it.colspan(2)
            }
            row()
            loginText = textField("", "login") {
                it.fill()
                it.width(200f)
                it.colspan(2)
            }
            row()
            pswdText = textField("", "password") {
                it.fill()
                it.width(200f)
                it.colspan(2)
            }
            row()
            loginBt = button {
                label("Connection")
                it.height(60f)
                it.padTop(10f)
                it.colspan(2)
            }
            row()
            registerBt = button {
                label("Create account")
                it.height(60f)
                it.padBottom(10f)
                it.colspan(2)
            }
        }
        loginText.messageText = "login"
        loginText.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                presenter.setLogin(loginText.text)
            }

        })
        pswdText.messageText = "password"
        pswdText.isPasswordMode = true
        pswdText.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                presenter.setPswd(pswdText.text)
            }

        })
        pswdText.setPasswordCharacter('*')
        rootTable.touchable = Touchable.childrenOnly
        loginBt.touchable = Touchable.enabled
        loginBt.isDisabled = true
        loginBt.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                if (!loginBt.isDisabled) {
                    presenter.onLogin()
                }
            }
        })
        registerBt.touchable = Touchable.enabled
        registerBt.isDisabled = true
        registerBt.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                if (!registerBt.isDisabled) {
                    presenter.onRegister()
                }
            }
        })

        errorMessageDialog = Dialog("Failure", Scene2DSkin.defaultSkin)
        errorMessageDialog.button("OK")
        try {
            val backgroundTex = Texture(getGameResource("login_back_image"))
            rootTable.background = TextureRegionDrawable(TextureRegion(backgroundTex))
        } catch (e: Exception) {
            logger.error("Can't load resources")
            logger.error(e.toString())
        }
    }

    fun showErrorMessage(msg: String) {
        errorMessageDialog.contentTable.clear()
        errorMessageDialog.text(msg)
        errorMessageDialog.show(stage)
    }

    override fun show() {
        stage += rootTable
        Gdx.input.inputProcessor = stage
        super.show()
    }

    override fun render(delta: Float) {
        stage.draw()
        stage.act()
    }

    override fun hide() {
        stage.clear()
        super.hide()
    }
}