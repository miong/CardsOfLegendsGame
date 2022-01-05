package com.bubul.col.tools

import com.bubul.col.game.core.utils.Cryptutils
import com.bubul.col.game.ui.getResourcePath
import java.io.File

fun main() {
    var isDecrypt = false;
    var cardsFile = File(getResourcePath("data/blob.enc"))
    val key = Cryptutils.parseAesKey(File(getResourcePath("cards.key")).readText())
    val data = if (cardsFile.exists()) {
        isDecrypt = true
        Cryptutils.decrypt(key, cardsFile.readText())
    } else {
        cardsFile = File(getResourcePath("data/cards.data"))
        Cryptutils.encrypt(key, cardsFile.readText())
    }
    val targetFile = if (isDecrypt) {
        File(getResourcePath("data/cards.data"))
    } else {
        File(getResourcePath("data/blob.enc"))
    }
    targetFile.writeText(data)
    cardsFile.delete()
}