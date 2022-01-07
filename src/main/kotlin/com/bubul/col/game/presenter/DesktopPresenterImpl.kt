package com.bubul.col.game.presenter

import com.badlogic.gdx.Gdx
import com.bubul.col.game.core.game.cards.CardManager
import com.bubul.col.game.core.game.cards.CardType
import com.bubul.col.game.core.net.*
import com.bubul.col.game.core.utils.LiveData
import com.bubul.col.game.core.utils.LiveDataListener
import com.bubul.col.game.ui.elements.cards.CardView
import com.bubul.col.game.ui.elements.cards.CardViewFactory
import com.bubul.col.game.ui.screens.DesktopScreen
import com.bubul.col.game.ui.screens.FriendStatus
import org.slf4j.LoggerFactory
import java.util.*

class DesktopPresenterImpl(val gamePresenter: GamePresenter, val screen: DesktopScreen) : DesktopPresenter {

    private var friends = mutableMapOf<String, LiveData<FriendStatus>>()
    private var friendRequests = mutableListOf<String>()
    private var friendFilter: String = ""
    private var friendManager: FriendManager? = null
    private var chatManager: ChatManager? = null
    private var lobbyManager: LobbyManager? = null
    private var loginManager: LoginManager? = null
    private var cardsManager: CardManager? = null
    private var currentChatDestination: String? = null
    private var serverPing: LiveData<Long>? = null
    private val logger = LoggerFactory.getLogger(this.javaClass.name)

    private var type = GameType.Normal
    private val invitesQueue: Queue<String> = LinkedList()
    private val cardViews = mutableMapOf<String, List<CardView>>()

    private val serverPingListener = object : LiveDataListener<Long> {
        override fun onChange(newValue: Long, oldValue: Long) {
            Gdx.app.postRunnable {
                if (newValue > 0)
                    screen.pingLabel.setText("$newValue ms")
                else
                    screen.pingLabel.setText("Inf.")
            }
        }
    }

    fun init() {
        Gdx.app.postRunnable {
            screen.setPresenter(this)
            screen.setUpdateText("Welcome to the new cards game\nwith MOBA aspect")
        }
    }

    fun dispose() {
        serverPing?.removeListener(serverPingListener)
    }

    override fun setPingToServer(ping: LiveData<Long>) {
        serverPing = ping
        serverPing!!.addListener(serverPingListener)
    }

    override fun sendMessage(target: String, msg: String) {
        if (msg.isEmpty())
            return
        chatManager!!.sendMessage(target, msg)
        Gdx.app.postRunnable {
            screen.addMessageItem(false, msg)
            screen.currentMessageTextField.text = ""
        }
    }

    override fun sendFriendRequest(target: String) {
        friendManager!!.sendFriendRequest(target)
    }

    override fun sendFriendRequestResponse(friendLogin: String, accepted: Boolean) {
        friendManager!!.sendFriendRequestResponse(friendLogin, accepted)
    }

    override fun filterFriends(pattern: String) {
        friendFilter = pattern
        val regex = Regex(".*${friendFilter.lowercase()}.*")
        Gdx.app.postRunnable {
            screen.clearFriendItems()
            for (friend in friends) {
                if (friendFilter.isEmpty() || regex.containsMatchIn(friend.key.lowercase())) {
                    screen.addFriendItem(friend.key, friend.value)
                }
            }
        }
    }

    private fun updateFriendRequests() {
        Gdx.app.postRunnable {
            screen.clearFriendRequestItems()
            for (friendRequest in friendRequests) {
                screen.addFriendRequestItem(friendRequest)
            }
        }
    }

    override fun setLobbyView(withOwnership: Boolean) {
        lobbyManager!!.setLobbyOpen(true, withOwnership)
        Gdx.app.postRunnable {
            if (type == GameType.Tutorial) {
                screen.setAdversaryName(lobbyManager!!.getAdversaryName())
            }
            screen.switchElemToLobby(type.name, loginManager!!.loggedId!!)
        }
    }

    override fun setGameTypeView() {
        lobbyManager!!.setLobbyOpen(false, false)
        Gdx.app.postRunnable { screen.switchElemToGameTypeSelection() }
    }

    override fun setUpdateView() {
        lobbyManager!!.setLobbyOpen(false, false)
        screen.resetLobby(lobbyManager!!.getAdversaryName())
        Gdx.app.postRunnable { screen.switchElemToUpdateView() }
    }

    override fun setLibraryView() {
        lobbyManager!!.setLobbyOpen(false, false)
        screen.resetLobby(lobbyManager!!.getAdversaryName())
        Gdx.app.postRunnable { screen.switchElemToLibraryView() }
    }

    override fun setCardManager(aCardManager: CardManager) {
        cardsManager = aCardManager
        Gdx.app.postRunnable { screen.initLibrary() }
    }

    override fun getRootCards(type: String): List<CardView> {
        if (cardViews.isEmpty()) {
            cardViews["Heroes"] = CardViewFactory.fromList(cardsManager!!.getCards()[CardType.Hero])
            cardViews["Spells"] = CardViewFactory.fromList(cardsManager!!.getCards()[CardType.InvocatorSpell])
            cardViews["Passives"] = CardViewFactory.fromList(cardsManager!!.getCards()[CardType.InvocatorPassive])
        }
        cardViews[type]?.let {
            return it!!
        }
        return listOf()
    }

    override fun setChatManager(aChatManager: ChatManager) {
        chatManager = aChatManager
    }

    override fun setLobbyManager(aLobbyManager: LobbyManager) {
        lobbyManager = aLobbyManager
        lobbyManager!!.canInvite().addListener(object : LiveDataListener<Boolean> {
            override fun onChange(newValue: Boolean, oldValue: Boolean) {
                Gdx.app.postRunnable { screen.setCanInvite(newValue) }
            }
        })
        lobbyManager!!.canStartGame().addListener(object : LiveDataListener<Boolean> {
            override fun onChange(newValue: Boolean, oldValue: Boolean) {
                Gdx.app.postRunnable { screen.setCanStartGame(newValue) }
            }
        })
    }

    override fun inviteFriend(name: String) {
        if (!lobbyManager!!.lobbyIsOpen()) {
            lobbyManager!!.setLobbyOpen(true, true)
            type = GameType.Normal
            setLobbyView(true)
        }
        lobbyManager!!.inviteFriend(name)
    }

    override fun getLobbyListener(): LobbyListener {
        return object : LobbyListener {
            override fun onLobbyInviteSent(name: String) {
                Gdx.app.postRunnable { screen.setInvitedAdversary(name) }
            }

            override fun onLobbyInvite(name: String) {
                addInvite(name)
            }

            override fun onInviteCancel(name: String) {
                removeInvite(name)
            }

            override fun onInviteRefused(name: String) {
                Gdx.app.postRunnable {
                    screen.setAdversaryName(lobbyManager!!.getAdversaryName())
                    screen.invitedAdversaryRefused(name)
                }
            }

            override fun onAdversaryArrival(name: String) {
                Gdx.app.postRunnable { screen.invitedAdversaryAccepted(name) }
            }

            override fun onAdversaryDeparture(name: String) {
                Gdx.app.postRunnable {
                    screen.setAdversaryName(lobbyManager!!.getAdversaryName())
                    screen.invitedAdversaryLeft(name)
                }
            }

            override fun onFullLobby(friendName: String) {
                // TODO?
                // for the moment only on invite possible so
                // not implemented yet. to be done for multiple invites if needed
            }

            override fun onGameFound() {
                Gdx.app.postRunnable { screen.showMatchMakingProposal() }
            }

            override fun onGameLaunched(adversaryId: String, serverSide: Boolean) {
                gamePresenter.startGame(type, adversaryId, serverSide)
            }

            override fun onGameCancelled() {
                Gdx.app.postRunnable { screen.showMatchMaking() }
            }

            override fun onGameProposalTimeout() {
                Gdx.app.postRunnable { screen.unshowMatchMaking() }
                lobbyManager!!.stopSearchingAdversary()
            }

        }
    }

    private fun removeInvite(name: String) {
        invitesQueue.remove(name)
        if (invitesQueue.isEmpty()) {
            Gdx.app.postRunnable { screen.unshowInvite() }
        }
    }

    private fun addInvite(name: String) {
        invitesQueue.add(name)
        if (invitesQueue.size == 1) {
            Gdx.app.postRunnable { screen.showInvite(name) }
        }
    }

    override fun onInviteResult(accepted: Boolean) {
        lobbyManager!!.sendInviteResponse(accepted)
        if (accepted) {
            val adversaryName = invitesQueue.poll()
            lobbyManager!!.cancelPending()
            invitesQueue.clear()
            Gdx.app.postRunnable {
                screen.unshowInvite()
                screen.setAdversaryName(adversaryName)
                setLobbyView(false)
            }
        } else {
            invitesQueue.poll()
            Gdx.app.postRunnable {
                if (!invitesQueue.isEmpty())
                    screen.showInvite(invitesQueue.element())
                else
                    screen.unshowInvite()
            }
        }
    }

    override fun isFriendOnline(name: String): Boolean {
        val status = friends[name] ?: return false
        return status.get().ordinal > FriendStatus.Offline.ordinal
    }

    override fun isFriendAvailable(name: String): Boolean {
        val status = friends[name] ?: return false
        return status.get() == FriendStatus.Online
    }

    override fun playGame() {
        if (lobbyManager!!.isFull())
            gamePresenter.startGame(type, lobbyManager!!.getAdversaryId(), lobbyManager!!.isLobbyOwner())
        else {
            lobbyManager!!.searchAdversary()
            screen.showMatchMaking()
        }

    }

    override fun stopSearchingAdversary() {
        lobbyManager!!.stopSearchingAdversary()
        screen.unshowMatchMaking()
    }

    override fun acceptGameProposal() {
        lobbyManager!!.acceptGameProposal()
    }

    override fun refuseGameProposal() {
        lobbyManager!!.refuseGameProposal()
        lobbyManager!!.stopSearchingAdversary()
        screen.unshowMatchMaking()
    }

    override fun onDraftAborted() {
        setUpdateView()
    }

    override fun getChatListener(): ChatListener {
        return object : ChatListener {
            override fun setMessages(messages: List<Pair<String, String>>) {
                Gdx.app.postRunnable {
                    screen.clearMessages()
                    for (msg in messages) {
                        screen.addMessageItem(msg.first == currentChatDestination, msg.second)
                    }
                }
            }

            override fun onMessage(sender: String, content: String) {
                logger.info("Treating message for $sender with current set to $currentChatDestination")
                if (sender == currentChatDestination) {
                    Gdx.app.postRunnable { screen.addMessageItem(true, content) }
                } else {
                    Gdx.app.postRunnable { screen.setHiddenMessages(sender) }
                }
            }

        }
    }

    override fun setFriendManager(aFriendManager: FriendManager) {
        friendManager = aFriendManager
    }

    private fun sortFriends() {
        val sortedMap = friends.toList().sortedWith(object : Comparator<Pair<String, LiveData<FriendStatus>>> {
            override fun compare(
                o1: Pair<String, LiveData<FriendStatus>>?,
                o2: Pair<String, LiveData<FriendStatus>>?
            ): Int {
                val o1Status = o1!!.second.get()
                val o2Status = o2!!.second.get()
                if (o1Status.ordinal != o2Status.ordinal)
                    return o1Status.ordinal - o2Status.ordinal
                return o1.first.lowercase().compareTo(o2.first.lowercase())
            }

        }).toMap()
        friends = sortedMap.toMutableMap()
    }

    private fun sortFriendRequests() {
        friendRequests.sort()
    }

    override fun getFriendListener(): FriendListener {
        return object : FriendListener {
            override fun onFriendLoaded(friends: List<FriendDesc>) {
                this@DesktopPresenterImpl.friends.clear()
                for (desc in friends) {
                    val status = when (desc.status) {
                        com.bubul.col.game.core.net.FriendStatus.Offline -> FriendStatus.Offline
                        com.bubul.col.game.core.net.FriendStatus.Online -> FriendStatus.Online
                        com.bubul.col.game.core.net.FriendStatus.Playing -> FriendStatus.Playing
                    }
                    this@DesktopPresenterImpl.friends[desc.name] = LiveData(status)
                }
                sortFriends()
                filterFriends(friendFilter)
            }

            override fun onFriendConnect(friend: String) {
                friends[friend]?.set(FriendStatus.Online)
                sortFriends()
                filterFriends(friendFilter)
            }

            override fun onFriendDisconnect(friend: String) {
                friends[friend]?.set(FriendStatus.Offline)
                sortFriends()
                filterFriends(friendFilter)
            }

            override fun onFriendRequest(friend: String) {
                friendRequests.add(friend)
                sortFriendRequests()
                updateFriendRequests()
            }

            override fun onFriendAdded(friend: FriendDesc) {
                val status = when (friend.status) {
                    com.bubul.col.game.core.net.FriendStatus.Offline -> FriendStatus.Offline
                    com.bubul.col.game.core.net.FriendStatus.Online -> FriendStatus.Online
                    com.bubul.col.game.core.net.FriendStatus.Playing -> FriendStatus.Playing
                }
                friends[friend.name] = LiveData(status)
                sortFriends()
                filterFriends(friendFilter)
            }

            override fun onFriendRemove(friend: String) {
                friends.remove(friend)
                sortFriends()
                filterFriends(friendFilter)
            }

            override fun onFriendStatusUpdate(friend: String, status: com.bubul.col.game.core.net.FriendStatus) {
                val uiStatus = when (status) {
                    com.bubul.col.game.core.net.FriendStatus.Offline -> FriendStatus.Offline
                    com.bubul.col.game.core.net.FriendStatus.Online -> FriendStatus.Online
                    com.bubul.col.game.core.net.FriendStatus.Playing -> FriendStatus.Playing
                }
                friends[friend]?.set(uiStatus)
                sortFriends()
                filterFriends(friendFilter)
            }

        }
    }

    override fun setupDesktop() {
        friends.clear()
        friendManager!!.loadFriends()
        loginManager!!.loggedId?.let { screen.usernameLabel.setText(it) }
    }

    override fun loadChatMessages(name: String) {
        logger.info("Load message for $name")
        currentChatDestination = name
        chatManager!!.loadMessages(name)
        Gdx.app.postRunnable {
            screen.chatUsernameLabel.setText(name)
            screen.chatTable.isVisible = true
        }
    }

    override fun setNoMessageActive() {
        currentChatDestination = null
    }

    override fun removeFriend(name: String) {
        friendManager!!.removeFriend(name)
    }

    override fun setLoginManager(aLoginManager: LoginManager) {
        loginManager = aLoginManager
    }

    override fun setGameType(type: GameType) {
        this.type = type
        if (type == GameType.Tutorial)
            lobbyManager!!.setTutorialMode()
    }
}