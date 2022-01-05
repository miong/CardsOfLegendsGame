package com.bubul.col.game.core.game.cards

import com.bubul.col.game.core.utils.CryptoException
import com.bubul.col.game.core.utils.Cryptutils
import com.bubul.col.game.ui.getResourcePath
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.slf4j.LoggerFactory
import java.io.File

class CardSerialization {

    private val logger = LoggerFactory.getLogger(this.javaClass.name)
    private val format = Json {
        classDiscriminator = "#class"
        allowSpecialFloatingPointValues = true
        prettyPrint = true
        serializersModule = SerializersModule {
            polymorphic(CardBase::class) {
                subclass(Hero::class, Hero.serializer())
                polymorphic(SpellBase::class) {
                    subclass(GrowingAttackSpell::class, GrowingAttackSpell.serializer())
                    subclass(StillAttackSpell::class, StillAttackSpell.serializer())
                }
            }
        }
    }

    fun loadCards(): Array<CardBase> {
        return try {
            val cardsFile = File(getResourcePath("data/blob.enc"))
            val pemFile = File(getResourcePath("cards.key"))
            val key = Cryptutils.parseAesKey(pemFile.readText())
            val data = Cryptutils.decrypt(key, cardsFile.readText())
            format.decodeFromString(data)
        } catch (e: CryptoException) {
            logger.error("Impossible to load cards")
            arrayOf()
        }
    }

    fun saveCards(cards: Array<CardBase>) {
        val cardsFile = File(getResourcePath("data/blob.enc"))
        val pemFile = File(getResourcePath("cards.key"))
        val key = Cryptutils.parseAesKey(pemFile.readText())
        val json = format.encodeToString(cards)
        val data = Cryptutils.encrypt(key, json)
        cardsFile.writeText(data)
    }
}