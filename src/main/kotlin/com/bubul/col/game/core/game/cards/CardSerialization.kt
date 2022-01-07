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
                subclass(InvocatorMoveSpell::class, InvocatorMoveSpell.serializer())
                subclass(InvocatorResourceSpell::class, InvocatorResourceSpell.serializer())
                subclass(InvocatorDefenceSpell::class, InvocatorDefenceSpell.serializer())
                subclass(InvocatorAttackSpell::class, InvocatorAttackSpell.serializer())
            }
            polymorphic(SpellBase::class) {
                // - StillSpell
                subclass(StillAttackSpell::class, StillAttackSpell.serializer())
                subclass(StillDefenseSpell::class, StillDefenseSpell.serializer())
                subclass(StillResourceSpell::class, StillResourceSpell.serializer())
                subclass(StillReinforcementSpell::class, StillReinforcementSpell.serializer())
                // - GrowingSpell
                subclass(GrowingAttackSpell::class, GrowingAttackSpell.serializer())
                subclass(GrowingDefenceSpell::class, GrowingDefenceSpell.serializer())
                subclass(GrowingResourceSpell::class, GrowingResourceSpell.serializer())
                subclass(GrowingReinforcementSpell::class, GrowingReinforcementSpell.serializer())
            }
            polymorphic(PassiveAptitude::class) {
                subclass(ResourcePassiveAptitude::class, ResourcePassiveAptitude.serializer())
                subclass(ReinforcementPassiveAptitude::class, ReinforcementPassiveAptitude.serializer())
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