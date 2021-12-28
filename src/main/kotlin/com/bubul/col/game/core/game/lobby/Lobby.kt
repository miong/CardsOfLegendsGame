package com.bubul.col.game.core.game.lobby

class Lobby {
    private var open = false
    private var isOwner = false
    private var adversary: String? = null
    private var adversaryID: String? = null

    fun open(withOwnership: Boolean) {
        isOwner = withOwnership
        open = true
    }

    fun close() {
        open = false
        isOwner = false
    }

    fun isOpen(): Boolean {
        return open
    }

    fun setAdversary(name: String, id: String) {
        adversary = name
        adversaryID = id
    }

    fun setTutorialMode() {
        adversary = "TutorialMaster"
        adversaryID = null
    }

    fun unsetAdversary() {
        adversary = null
        adversaryID = null
    }

    fun isFull(): Boolean {
        return adversary != null
    }

    fun getAdversaryName(): String {
        adversary?.let {
            return it
        }
        return "???????"
    }

    fun getAdversaryId(): String? {
        return adversaryID
    }

    fun isOwner(): Boolean {
        return isOwner
    }

}