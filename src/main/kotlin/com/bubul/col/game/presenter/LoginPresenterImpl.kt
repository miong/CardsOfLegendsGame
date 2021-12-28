package com.bubul.col.game.presenter

import com.bubul.col.game.core.net.LoginListener
import com.bubul.col.game.core.net.LoginManager
import com.bubul.col.game.core.net.LoginStatus
import com.bubul.col.game.core.utils.LiveData
import com.bubul.col.game.core.utils.LiveDataListener
import com.bubul.col.game.ui.screens.LoginScreen

class LoginPresenterImpl(val game: GamePresenter, private val screen: LoginScreen) : LoginPresenter {

    private var login = ""
    private var pswd = ""
    private var canConnect = false
    private var serverPing: LiveData<Long>? = null
    private var loginManager: LoginManager? = null

    private val serverPingListener = object : LiveDataListener<Long> {
        override fun onChange(newValue: Long, oldValue: Long) {
            if (newValue > 0)
                screen.pingLabel.setText("$newValue ms")
            else
                screen.pingLabel.setText("inf.")
            canConnect = newValue > 0
            updateButtonState()
        }
    }

    fun init() {
        screen.presenter = this
    }

    fun dispose() {
        serverPing?.removeListener(serverPingListener)
    }

    override fun setPingToServer(ping: LiveData<Long>) {
        serverPing = ping
        serverPing!!.addListener(serverPingListener)
    }

    override fun setLogin(messageText: String) {
        login = messageText
        updateButtonState()
    }

    override fun setPswd(messageText: String) {
        pswd = messageText
        updateButtonState()
    }

    override fun getLoginListener(): LoginListener {
        return object : LoginListener {
            override fun onLoginResult(result: LoginStatus) {
                if (result == LoginStatus.Failed) {
                    screen.showErrorMessage("Failed to login. Verify your ID and password and that you're not already connected.")
                } else {
                    game.showDesktop()
                }
            }

            override fun onRegisterResult(result: LoginStatus) {
                if (result == LoginStatus.Failed) {
                    screen.showErrorMessage("Failed to register. This login is already used by someone else.")
                } else {
                    game.showDesktop()
                }
            }

        }
    }

    override fun setLoginManager(aLoginManager: LoginManager) {
        loginManager = aLoginManager
    }

    override fun onLogin() {
        loginManager!!.login(login, pswd)
    }

    override fun onRegister() {
        loginManager!!.register(login, pswd)
    }

    private fun updateButtonState() {
        val disabled: Boolean = login.isEmpty() || pswd.isEmpty() || !canConnect
        screen.loginBt.isDisabled = disabled
        screen.registerBt.isDisabled = disabled
    }
}