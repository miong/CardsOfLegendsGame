package com.bubul.col.game.presenter

import com.badlogic.gdx.Gdx
import com.bubul.col.game.core.game.cards.CardManager
import com.bubul.col.game.core.net.*
import com.bubul.col.game.core.utils.LiveData
import com.bubul.col.game.ui.UiGame
import com.bubul.col.game.ui.elements.cards.CardView
import org.slf4j.LoggerFactory

enum class GameType {
    Normal,
    Tutorial
}

interface LoginPresenter {
    fun setPingToServer(ping: LiveData<Long>)
    fun setLogin(messageText: String)
    fun setPswd(messageText: String)
    fun getLoginListener(): LoginListener
    fun onLogin()
    fun onRegister()
    fun setLoginManager(aLoginManager: LoginManager)
    fun showInitializationError()
    fun forceQuitAndUpload()
    fun gameInitDone()
}

interface DesktopPresenter {
    fun sendMessage(target: String, msg: String)
    fun sendFriendRequest(target: String)
    fun filterFriends(pattern: String)
    fun setLobbyView(withOwnership: Boolean)
    fun setGameTypeView()
    fun setUpdateView()
    fun setChatManager(aChatManager: ChatManager)
    fun getChatListener(): ChatListener
    fun setFriendManager(aFriendManager: FriendManager)
    fun getFriendListener(): FriendListener
    fun setupDesktop()
    fun loadChatMessages(name: String)
    fun setLoginManager(aLoginManager: LoginManager)
    fun setPingToServer(ping: LiveData<Long>)
    fun sendFriendRequestResponse(friendLogin: String, accepted: Boolean)
    fun setNoMessageActive()
    fun removeFriend(name: String)
    fun setGameType(type: GameType)
    fun setLobbyManager(aLobbyManager: LobbyManager)
    fun inviteFriend(name: String)
    fun getLobbyListener(): LobbyListener
    fun isFriendOnline(name: String): Boolean
    fun isFriendAvailable(name: String): Boolean
    fun playGame()
    fun onInviteResult(accepted: Boolean)
    fun stopSearchingAdversary()
    fun acceptGameProposal()
    fun refuseGameProposal()
    fun onDraftAborted()
    fun setLibraryView()
    fun setCardManager(aCardManager: CardManager)
    fun getRootCards(type: String): List<CardView>

}

interface DraftPresenter {
    fun draftAborted()
}

class GamePresenter(private val uiGame: UiGame) {

    private val logger = LoggerFactory.getLogger(this.javaClass.name)
    val loginPresenter = LoginPresenterImpl(this, uiGame.loginScreen)
    val desktopPresenter = DesktopPresenterImpl(this, uiGame.desktopScreen)
    val draftPresenter = DraftPresenterImpl(this, uiGame.draftScreen)
    private var desktopSetup = false

    fun init() {
        loginPresenter.init()
        desktopPresenter.init()
        draftPresenter.init()
    }

    fun dispose() {
        loginPresenter.dispose()
        desktopPresenter.dispose()
        draftPresenter.dispose()
    }

    fun showDesktop() {
        if (!desktopSetup) {
            logger.info("setup desktop")
            desktopPresenter.setupDesktop()
            desktopSetup = true
        }
        logger.info("show desktop")
        Gdx.app.postRunnable { uiGame.showDesktop() }
    }

    fun startGame(type: GameType, adversaryId: String, serverSide: Boolean) {
        logger.info("start $type game against $adversaryId as server $serverSide")
        Gdx.app.postRunnable { uiGame.showDraft() }
    }

    fun draftAborted() {
        desktopPresenter.onDraftAborted()
        showDesktop()
    }

    fun showInitializationError() {
        loginPresenter.showInitializationError()
    }

    fun quitGame() {
        uiGame.quit()
    }

    fun GameInitDone() {
        loginPresenter.gameInitDone()
    }

}