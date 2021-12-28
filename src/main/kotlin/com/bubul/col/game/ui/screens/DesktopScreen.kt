package com.bubul.col.game.ui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.bubul.col.game.core.utils.DecomposedGifSprite
import com.bubul.col.game.core.utils.LiveData
import com.bubul.col.game.core.utils.LiveDataListener
import com.bubul.col.game.core.utils.loadDecomposedGif
import com.bubul.col.game.presenter.DesktopPresenter
import com.bubul.col.game.ui.elements.desktop.DesktopGameTypeSelectorElem
import com.bubul.col.game.ui.elements.desktop.DesktopLobbyElem
import com.bubul.col.game.ui.elements.desktop.DesktopUpdatesElem
import com.bubul.col.game.ui.getGameResource
import ktx.actors.plusAssign
import ktx.actors.stage
import ktx.app.KtxScreen
import ktx.scene2d.*
import org.slf4j.LoggerFactory

enum class FriendStatus {
    Offline,
    Online,
    Playing
}

class DesktopScreen : KtxScreen {

    private val ENTER_KEY: Char = 13.toChar()

    private lateinit var screenStage: Stage
    private lateinit var rootTable: Table
    private lateinit var headTable: Table
    private lateinit var contentTable: Table
    private lateinit var containerTable: Table
    private lateinit var updateElem: DesktopUpdatesElem
    private lateinit var gameTypeSelectorElem: DesktopGameTypeSelectorElem
    private lateinit var lobbyElem: DesktopLobbyElem
    lateinit var pingLabel: Label
    lateinit var usernameLabel: Label
    private lateinit var friendTable: Table
    lateinit var chatTable: Table
    private lateinit var messageContainer: Table
    lateinit var chatUsernameLabel: Label
    private lateinit var friendListTable: Table
    private lateinit var friendRequestsListTable: Table
    lateinit var currentMessageTextField: TextField
    private var addFriendWindow: Window? = null
    private var friendRequestWindow: Window? = null
    private var rightClickFriendWindow: Window? = null
    private var validateRemoveFriendWindow: Window? = null
    private var inviteWindow: Window? = null
    private lateinit var inviteAdversaryNameLabel: Label
    private lateinit var friendRequestNbLabel: Label
    private lateinit var playBtn: Button
    private lateinit var inviteButton: Button

    private var matchMakingWindow: Window? = null
    private lateinit var matchMakingContainerTable: Table
    private lateinit var matchMakingSearchingTable: Table
    private lateinit var matchMakingProposalTable: Table
    private lateinit var matchMakingProposalHeartTable: Table
    private lateinit var matchMakingProposalTimerTable: Table
    private lateinit var goButton: Button
    private lateinit var nopeButton: Button

    private var friendRequestNb = 0
    private var canInvite = true

    private lateinit var presenter: DesktopPresenter
    private val logger = LoggerFactory.getLogger(this.javaClass.name)

    fun init() {
        screenStage = stage()
        updateElem = DesktopUpdatesElem()
        rootTable = scene2d.table {
            setFillParent(true)
            touchable = Touchable.childrenOnly
            row()
            headTable = table {
                playBtn = button {
                    label("Play")
                    addListener(object : ClickListener() {
                        override fun clicked(event: InputEvent?, x: Float, y: Float) {
                            presenter.setGameTypeView()
                        }
                    })
                    it.left()
                    it.width(100f)
                    it.top()
                    it.left()
                }
                label("Ping : ", "gold-title") {
                    it.padLeft(10f)
                }.apply {
                    this.fontScaleY = 0.6f
                    this.fontScaleX = 0.6f
                }
                pingLabel = label("Inf.", "gold-title") {
                    it.right()
                    it.padRight(10f)
                }.apply {
                    this.fontScaleY = 0.6f
                    this.fontScaleX = 0.6f
                }
                usernameLabel = label("Username", "gold-title") {
                    it.expandX()
                    it.right()
                    it.padRight(10f)
                }.apply {
                    this.fontScaleY = 0.6f
                    this.fontScaleX = 0.6f
                }
                touchable = Touchable.childrenOnly
                it.expandX()
                it.fillX()
                it.colspan(2)
            }
            row()
            contentTable = table {
                touchable = Touchable.childrenOnly
                containerTable = table {
                    add(updateElem.getUI()).expand().fill()
                    it.expand()
                    it.fill()
                }
                chatTable = table {
                    val pixmap = Pixmap(20, 20, Pixmap.Format.RGBA8888).apply {
                        setColor(0f, 0f, 0f, 0.5f)
                        fill()
                    }
                    background = TextureRegionDrawable(TextureRegion(Texture(pixmap)))
                    touchable = Touchable.childrenOnly
                    chatUsernameLabel = label("", "gold-title") {
                        it.expandX()
                        it.fillX()
                        setAlignment(Align.center)
                    }.apply {
                        this.fontScaleY = 0.6f
                        this.fontScaleX = 0.6f
                    }
                    button {
                        image(TextureRegionDrawable(TextureRegion(Texture(getGameResource("icons/icons8-subtract-30.png")))))
                        it.size(50f)
                        addListener(object : ClickListener() {
                            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                                chatTable.isVisible = false
                                currentMessageTextField.text = ""
                                presenter.setNoMessageActive()
                            }
                        })
                        touchable = Touchable.enabled
                    }
                    row()
                    messageContainer = table {
                        it.colspan(2)
                        it.expand().fillX()
                        it.top().left()
                    }
                    row()
                    currentMessageTextField = textField {
                        it.expandX()
                        it.fillX()
                        it.colspan(2)
                    }.apply {
                        setTextFieldListener { _, c ->
                            if (c == ENTER_KEY) {
                                presenter.sendMessage(chatUsernameLabel.text.toString(), currentMessageTextField.text)
                            }
                        }
                    }
                    it.width(200f)
                    it.expandY()
                    it.fillY()
                }
                it.expand()
                it.fill()
            }
            friendTable = table {
                label("Social", "gold-title") {
                    it.fillX()
                    setAlignment(Align.center)
                }.apply {
                    this.fontScaleY = 0.6f
                    this.fontScaleX = 0.6f
                }
                button {
                    image(TextureRegionDrawable(TextureRegion(Texture(getGameResource("icons/flaticon_income.png"))))) {
                        it.size(20f)
                        it.padRight(3f)
                    }
                    addListener(object : ClickListener() {
                        override fun clicked(event: InputEvent?, x: Float, y: Float) {
                            screenStage.addActor(friendRequestWindow)
                            friendRequestWindow!!.pack()
                            friendRequestWindow!!.setPosition(
                                screenStage.width / 2 - friendRequestWindow!!.width / 2,
                                screenStage.height / 2 - friendRequestWindow!!.height / 2
                            )
                        }
                    })
                    friendRequestNbLabel = label(friendRequestNb.toString(), "gold-title") {
                        it.size(7f)
                    }.apply {
                        this.setAlignment(Align.left)
                        this.fontScaleY = 0.4f
                        this.fontScaleX = 0.4f
                    }
                    it.width(70f)
                    it.height(50f)
                }.apply {
                    this.align(Align.left)
                }
                button {
                    image(TextureRegionDrawable(TextureRegion(Texture(getGameResource("icons/icons8-plus-+-24.png")))))
                    it.width(50f)
                    it.height(50f)
                    addListener(object : ClickListener() {
                        override fun clicked(event: InputEvent?, x: Float, y: Float) {
                            if (addFriendWindow != null)
                                return
                            addFriendWindow = scene2d.window("") {
                                table {
                                    table {
                                        label("Add a friend") {
                                            it.expand().fill()
                                        }
                                        button {
                                            label("X")
                                            addListener(object : ClickListener() {
                                                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                                                    addFriendWindow!!.remove()
                                                    addFriendWindow = null
                                                }
                                            })
                                            it.width(60f)
                                        }
                                        it.expand().fill()
                                        it.colspan(3)
                                    }
                                    row()
                                    label("name : ")
                                    val newFriendName = textField {
                                        it.expandX().fillX()
                                    }
                                    button {
                                        image(TextureRegionDrawable(TextureRegion(Texture(getGameResource("icons/icons8-plus-+-24.png")))))
                                        addListener(object : ClickListener() {
                                            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                                                presenter.sendFriendRequest(newFriendName.text)
                                                newFriendName.text = ""
                                            }
                                        })
                                        it.width(60f)
                                    }
                                }
                            }.apply {
                                screenStage += this
                                setSize(300f, 100f)
                                setPosition(screenStage.width / 2 - width / 2, screenStage.height / 2 - height / 2)
                                show()
                            }
                        }
                    })
                }
                row()
                label("search", "gold-title") {
                    it.expandX().fillX().colspan(3)
                }.apply {
                    this.setAlignment(Align.center)
                    this.fontScaleY = 0.6f
                    this.fontScaleX = 0.6f
                }
                row()
                textField {
                    it.expandX().fillX().colspan(3)
                }.apply {
                    this.addListener(object : ChangeListener() {
                        override fun changed(event: ChangeEvent?, actor: Actor?) {
                            val tf: TextField = actor as TextField
                            presenter.filterFriends(tf.text)
                        }
                    })
                }
                row()
                scrollPane {
                    friendListTable = table { _ ->
                        align(Align.top)
                        it.expand()
                        it.fill()
                    }
                    it.expand()
                    it.fill()
                    it.colspan(3)
                    setScrollbarsVisible(true)
                    setForceScroll(false, true)
                    setScrollingDisabled(true, false)
                    fadeScrollBars = false
                }
                it.width(200f)
                it.expandY()
                it.fillY()
            }
        }
        friendRequestWindow = scene2d.window("") {
            table {
                table {
                    label("Friend requests") {
                        it.expand().fill()
                    }
                    button {
                        label("X")
                        addListener(object : ClickListener() {
                            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                                friendRequestWindow!!.remove()
                            }
                        })
                        it.width(60f)
                    }
                    it.expand().fill()
                }
                row()
                scrollPane {
                    friendRequestsListTable = table { _ ->
                        align(Align.top)
                        it.expand()
                        it.fill()
                    }
                    it.expand().fill()
                    it.maxHeight(300f)
                    it.maxWidth(300f)
                }.apply {
                    this.fadeScrollBars = false
                    this.setScrollbarsVisible(true)
                }
            }
        }
        playBtn.isDisabled = true
        chatTable.isVisible = false
        val blueBackPixmap = Pixmap(16, 16, Pixmap.Format.RGBA8888)
        blueBackPixmap.setColor(Color.FIREBRICK)
        blueBackPixmap.fill()
        friendTable.background = TextureRegionDrawable(TextureRegion(Texture(blueBackPixmap)))
        try {
            val backgroundTex = Texture(getGameResource("login_back_image"))
            contentTable.background = TextureRegionDrawable(TextureRegion(backgroundTex))
        } catch (e: Exception) {
            logger.error("Can't load resources")
            logger.error(e.toString())
        }
    }

    fun addFriendItem(name: String, status: LiveData<FriendStatus>) {
        val pixmap = Pixmap(16, 16, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE)
        pixmap.fillCircle(7, 7, 4)
        val texture = Texture(pixmap)
        var statusLogo: Image?

        val pixmap2 = Pixmap(16, 16, Pixmap.Format.RGBA8888)
        pixmap2.setColor(Color.WHITE)
        pixmap2.fillCircle(7, 7, 4)
        val texture2 = Texture(pixmap2)
        var messageStatusLogo: Image?

        val item = scene2d.table {
            statusLogo = image(texture)
            label(name, "gold-title") {
                it.expand()
                it.fill()
                it.padLeft(5f)
                it.padTop(5f)
                setAlignment(Align.left)
            }.apply {
                this.fontScaleY = 0.6f
                this.fontScaleX = 0.6f
            }
            messageStatusLogo = image(texture2)
            it.height = 20f
        }
        when (status.get()) {
            FriendStatus.Offline -> {
                statusLogo!!.color = Color.GRAY
            }
            FriendStatus.Online -> {
                statusLogo!!.color = Color.GREEN
            }
            FriendStatus.Playing -> {
                statusLogo!!.color = Color.RED
            }
        }
        item.touchable = Touchable.enabled
        item.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                if (statusLogo!!.color == Color.GREEN) {
                    presenter.loadChatMessages(name)
                    messageStatusLogo!!.color = Color.CLEAR
                }
            }
        })
        item.addListener(object : ClickListener(Input.Buttons.RIGHT) {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                if (rightClickFriendWindow != null) {
                    rightClickFriendWindow!!.remove()
                }
                rightClickFriendWindow = scene2d.window("") {
                    table {
                        button {
                            label("Message")
                        }.apply {
                            addListener(object : ClickListener() {
                                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                                    if (isDisabled)
                                        return
                                    rightClickFriendWindow!!.remove()
                                    rootTable.touchable = Touchable.childrenOnly
                                    presenter.loadChatMessages(name)
                                    messageStatusLogo!!.color = Color.CLEAR
                                }
                            })
                            isDisabled = !presenter.isFriendOnline(name)
                        }
                        row()
                        inviteButton = button {
                            label("Invite")
                        }.apply {
                            addListener(object : ClickListener() {
                                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                                    if (isDisabled)
                                        return
                                    rightClickFriendWindow!!.remove()
                                    presenter.inviteFriend(name)
                                }
                            })
                            isDisabled = !presenter.isFriendAvailable(name)
                        }
                        row()
                        button {
                            label("Remove")
                        }.apply {
                            addListener(object : ClickListener() {
                                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                                    if (isDisabled)
                                        return
                                    rightClickFriendWindow!!.remove()
                                    validateRemoveFriendWindow = scene2d.window("") {
                                        table {
                                            label("Are you sure you want to remove $name from your friends") {
                                                it.colspan(2)
                                            }
                                            row()
                                            button {
                                                label("Yes")
                                            }.apply {
                                                addListener(object : ClickListener() {
                                                    override fun clicked(event: InputEvent?, x: Float, y: Float) {
                                                        validateRemoveFriendWindow!!.remove()
                                                        rootTable.touchable = Touchable.childrenOnly
                                                        presenter.removeFriend(name)
                                                    }
                                                })
                                            }
                                            button {
                                                label("No")
                                            }.apply {
                                                addListener(object : ClickListener() {
                                                    override fun clicked(event: InputEvent?, x: Float, y: Float) {
                                                        validateRemoveFriendWindow!!.remove()
                                                        rootTable.touchable = Touchable.childrenOnly
                                                    }
                                                })
                                            }
                                        }
                                    }.apply {
                                        pack()
                                        setPosition(rootTable.width / 2 - width / 2, rootTable.height / 2 - height / 2)
                                    }
                                    screenStage.addActor(validateRemoveFriendWindow!!)
                                    validateRemoveFriendWindow!!.isMovable = false
                                }
                            })
                        }
                    }
                }.apply {
                    pack()
                    val pos = item.localToStageCoordinates(Vector2(x, y))
                    setPosition(pos.x - width, pos.y - height)
                }
                rootTable.touchable = Touchable.enabled
                rootTable.addListener(object : ClickListener() {
                    override fun clicked(event: InputEvent?, x: Float, y: Float) {
                        rightClickFriendWindow?.remove()
                        validateRemoveFriendWindow?.remove()
                        rootTable.touchable = Touchable.childrenOnly
                    }
                })
                screenStage.addActor(rightClickFriendWindow)
                rightClickFriendWindow!!.isMovable = false
                inviteButton.isDisabled = !canInvite || !presenter.isFriendAvailable(name)
            }
        })
        status.addListener(object : LiveDataListener<FriendStatus> {
            override fun onChange(newValue: FriendStatus, oldValue: FriendStatus) {
                if (newValue == FriendStatus.Offline) {
                    statusLogo!!.color = Color.GRAY
                } else {
                    statusLogo!!.color = Color.GREEN
                }
                pixmap.fillCircle(7, 7, 4)
            }
        })

        messageStatusLogo!!.color = Color.CLEAR

        friendListTable.add(item).expandX().fill().top().row()
    }

    fun clearFriendItems() {
        friendListTable.clear()
    }

    fun setHiddenMessages(name: String) {
        for (child in friendListTable.children) {
            val item = child as Table
            val nameLabel = item.getChild(1) as Label
            if (nameLabel.text.toString() == name) {
                val messageStatusLogo = item.getChild(2)
                messageStatusLogo.color = Color.YELLOW
                val wavSound: Sound = Gdx.audio.newSound(getGameResource("sounds/new_message.wav"))
                wavSound.play(0.2f)
                return
            }
        }
    }

    override fun show() {
        screenStage += rootTable
        Gdx.input.inputProcessor = screenStage
        super.show()
    }

    override fun render(delta: Float) {
        screenStage.draw()
        screenStage.act()
    }

    override fun hide() {
        screenStage.clear()
        super.hide()
    }

    fun setPresenter(aPresenter: DesktopPresenter) {
        presenter = aPresenter
        Gdx.app.postRunnable {
            gameTypeSelectorElem = DesktopGameTypeSelectorElem(presenter)
            lobbyElem = DesktopLobbyElem(presenter)
            playBtn.isDisabled = false
        }
    }

    fun switchElemToGameTypeSelection() {
        containerTable.clear()
        containerTable.add(gameTypeSelectorElem.getUI()).expand().fill()
    }

    fun switchElemToUpdateView() {
        containerTable.clear()
        containerTable.add(updateElem.getUI()).expand().fill()
    }

    fun switchElemToLobby(typeName: String, myName: String) {
        lobbyElem.setTypeOfGame(typeName)
        lobbyElem.setMyName(myName)
        containerTable.clear()
        containerTable.add(lobbyElem.getUI()).expand().fill()
    }

    fun clearMessages() {
        messageContainer.clear()
    }

    fun addMessageItem(leftAlign: Boolean, content: String) {
        val alignment = if (leftAlign) Align.left else Align.right
        val color = if (leftAlign) Color.BROWN else Color.FIREBRICK
        val msgLabel = scene2d.label(" $content ") {
            setAlignment(alignment)
            wrap = true

        }
        val labelColor = Pixmap(10, 10, Pixmap.Format.RGB888)
        labelColor.setColor(color)
        labelColor.fill()
        val style = Label.LabelStyle()
        style.font = msgLabel.style.font
        style.fontColor = Color.WHITE
        style.background = Image(Texture(labelColor)).drawable
        msgLabel.style = style
        messageContainer.add(msgLabel).expandX().fillX().padBottom(5f).padLeft(5f).padRight(5f).row()
    }

    fun clearFriendRequestItems() {
        friendRequestsListTable.clear()
        friendRequestNb = 0
    }

    fun addFriendRequestItem(friendRequest: String) {
        friendRequestNb++
        friendRequestNbLabel.setText(friendRequestNb)
        val item = scene2d.table {
            val itemActor = it
            label(friendRequest, "gold-title") {
                it.expand()
                it.fill()
                it.padLeft(5f)
                it.padTop(5f)
                setAlignment(Align.left)
            }.apply {
                this.fontScaleY = 0.6f
                this.fontScaleX = 0.6f
            }
            button {
                image(TextureRegionDrawable(TextureRegion(Texture(getGameResource("icons/icons8-plus-+-24.png")))))
                it.width(50f)
                it.height(50f)
                addListener(object : ClickListener() {
                    override fun clicked(event: InputEvent?, x: Float, y: Float) {
                        presenter.sendFriendRequestResponse(friendRequest, true)
                        friendRequestsListTable.removeActor(itemActor)
                        friendRequestNb--
                        friendRequestNbLabel.setText(friendRequestNb)
                    }
                })
            }
            button {
                image(TextureRegionDrawable(TextureRegion(Texture(getGameResource("icons/icons8-subtract-30.png")))))
                it.width(50f)
                it.height(50f)
                addListener(object : ClickListener() {
                    override fun clicked(event: InputEvent?, x: Float, y: Float) {
                        presenter.sendFriendRequestResponse(friendRequest, false)
                        friendRequestsListTable.removeActor(itemActor)
                        friendRequestNb--
                        friendRequestNbLabel.setText(friendRequestNb)
                    }
                })
            }
            it.height = 50f
        }
        friendRequestsListTable.add(item).expandX().fill().top().row()
    }

    fun setUpdateText(s: String) {
        updateElem.setUpdateTxt(s)
    }

    fun setCanInvite(canInvite: Boolean) {
        this.canInvite = canInvite
    }

    fun setCanStartGame(canStartGame: Boolean) {
        lobbyElem.setCanStartGame(canStartGame)
    }

    fun setInvitedAdversary(name: String) {
        lobbyElem.setInvitedAdversary(name)
    }

    fun invitedAdversaryRefused(name: String) {
        lobbyElem.invitedAdversaryRefused(name)
    }

    fun invitedAdversaryAccepted(name: String) {
        lobbyElem.invitedAdversaryAccepted(name)
    }

    fun invitedAdversaryLeft(name: String) {
        lobbyElem.invitedAdversaryLeft(name)
    }

    fun setAdversaryName(name: String) {
        lobbyElem.setAdversaryName(name)
    }

    fun showInvite(name: String) {
        if (inviteWindow == null) {
            inviteWindow = scene2d.window("") {
                table {
                    inviteAdversaryNameLabel = label("$name invite you to play", "gold-title") {
                        it.colspan(2)
                    }
                    row()
                    button {
                        label("Accept")
                    }.apply {
                        addListener(object : ClickListener() {
                            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                                presenter.onInviteResult(true)
                            }
                        })
                    }
                    button {
                        label("Refuse")
                    }.apply {
                        addListener(object : ClickListener() {
                            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                                presenter.onInviteResult(false)
                            }
                        })
                    }

                }
            }
        } else {
            inviteAdversaryNameLabel.setText("$name invite you to play")
        }
        inviteWindow!!.apply {
            isModal = true
            isMovable = false
            pack()
            setPosition(rootTable.width / 2 - width / 2, rootTable.height / 2 - height / 2)
        }
        screenStage.addActor(inviteWindow)
    }

    fun unshowInvite() {
        inviteWindow?.remove()
    }

    fun resetLobby(adversaryName: String) {
        lobbyElem.resetLobby(adversaryName)
    }

    fun showMatchMaking() {
        if (matchMakingWindow == null) {
            matchMakingWindow = scene2d.window("") {
                matchMakingContainerTable = table {
                    it.expand().fill()
                }
            }
            matchMakingSearchingTable = scene2d.table {
                label("Searching a game", "gold-title")
                row()
                image(loadDecomposedGif("icons/icons8-dots-loading-gif_decomposed", "icons8-dots-loading", 28, 30f)) {
                    setSize(70f, 70f)
                    color = Color.CORAL
                }
                row()
                button {
                    label("Cancel")
                    it.width(170f)
                    it.height(60f)
                }.apply {
                    addListener(object : ClickListener() {
                        override fun clicked(event: InputEvent?, x: Float, y: Float) {
                            presenter.stopSearchingAdversary()
                        }
                    })
                }
            }
            matchMakingProposalTable = scene2d.table {
                label("Game found", "gold-title") {
                    it.colspan(2)
                }
                row()
                matchMakingProposalTimerTable = table {
                    matchMakingProposalHeartTable = table {
                        image(
                            loadDecomposedGif(
                                "icons/icons8-loading-heart-gif_decomposed",
                                "icons8-loading-heart",
                                15,
                                30f,
                                0f,
                                false
                            )
                        ) {
                            setSize(50f, 50f)
                        }
                        image(
                            loadDecomposedGif(
                                "icons/icons8-loading-heart-gif_decomposed",
                                "icons8-loading-heart",
                                15,
                                30f,
                                1f,
                                false
                            )
                        ) {
                            setSize(50f, 50f)
                        }
                        image(
                            loadDecomposedGif(
                                "icons/icons8-loading-heart-gif_decomposed",
                                "icons8-loading-heart",
                                15,
                                30f,
                                2f,
                                false
                            )
                        ) {
                            setSize(50f, 50f)
                        }
                        image(
                            loadDecomposedGif(
                                "icons/icons8-loading-heart-gif_decomposed",
                                "icons8-loading-heart",
                                15,
                                30f,
                                3f,
                                false
                            )
                        ) {
                            setSize(50f, 50f)
                        }
                        image(
                            loadDecomposedGif(
                                "icons/icons8-loading-heart-gif_decomposed",
                                "icons8-loading-heart",
                                15,
                                30f,
                                4f,
                                false
                            )
                        ) {
                            setSize(50f, 50f)
                        }
                    }
                    it.colspan(2)
                }
                row()
                goButton = button {
                    label("Go", "gold-title")
                    it.width(170f)
                    it.height(60f)
                }.apply {
                    addListener(object : ClickListener() {
                        override fun clicked(event: InputEvent?, x: Float, y: Float) {
                            if (isDisabled)
                                return
                            isDisabled = true
                            nopeButton.isDisabled = true
                            matchMakingProposalHeartTable.remove()
                            matchMakingProposalTimerTable.add(
                                scene2d.image(TextureRegionDrawable(TextureRegion(Texture(getGameResource("icons/icons8-approbation-48.png")))))
                            )
                            presenter.acceptGameProposal()
                        }
                    })
                }
                nopeButton = button {
                    label("Nope", "gold-title")
                    it.width(170f)
                    it.height(60f)
                }.apply {
                    addListener(object : ClickListener() {
                        override fun clicked(event: InputEvent?, x: Float, y: Float) {
                            if (isDisabled)
                                return
                            isDisabled = true
                            goButton.isDisabled = true
                            presenter.refuseGameProposal()
                        }
                    })
                }
            }
        }
        matchMakingProposalTable.remove()
        matchMakingContainerTable.add(matchMakingSearchingTable)
        matchMakingWindow!!.apply {
            isModal = true
            isMovable = false
            setSize(600f, 200f)
            setPosition(rootTable.width / 2 - width / 2, rootTable.height / 2 - height / 2)
        }
        screenStage.addActor(matchMakingWindow!!)
    }

    fun unshowMatchMaking() {
        matchMakingWindow?.remove()
    }

    fun showMatchMakingProposal() {
        goButton.isDisabled = false
        nopeButton.isDisabled = false
        matchMakingSearchingTable.remove()
        matchMakingProposalTimerTable.clear()
        matchMakingProposalTimerTable.add(matchMakingProposalHeartTable)
        for (actor in matchMakingProposalHeartTable.children) {
            val image = actor as Image
            val spriteDrawable = image.drawable as SpriteDrawable
            val gifSprite = spriteDrawable.sprite as DecomposedGifSprite
            gifSprite.resetTimer()

        }
        matchMakingContainerTable.add(matchMakingProposalTable)
    }


}