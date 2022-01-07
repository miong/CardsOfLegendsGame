package com.bubul.col.game.core.game.cards

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface CardBase {
    val id: String
    val name: String
    val description: String
    val picturePath: String
}

enum class CardType {
    Hero,
    HeroSpell,
    HeroPassive,
    InvocatorSpell,
    InvocatorPassive
}

interface SpellBase : CardBase {
    val tick: Int
    val spiritCost: Int
}

interface GrowingSpell : SpellBase {
    val lightFactor: Float
    val darkFactor: Float
}

interface StillSpell : SpellBase

interface AttackSpellBase : SpellBase {
    val baseDamage: Int
    val baseInjury: Int
}

@Serializable
@SerialName("GrowingAttackSpell")
data class GrowingAttackSpell(
    override val id: String,
    override val name: String,
    override val description: String,
    override val picturePath: String,
    override val tick: Int,
    override val spiritCost: Int,
    override val lightFactor: Float,
    override val darkFactor: Float,
    override val baseDamage: Int,
    override val baseInjury: Int,
) : GrowingSpell, AttackSpellBase

@Serializable
@SerialName("StillAttackSpell")
data class StillAttackSpell(
    override val id: String,
    override val name: String,
    override val description: String,
    override val picturePath: String,
    override val tick: Int,
    override val spiritCost: Int,
    override val baseDamage: Int,
    override val baseInjury: Int
) : StillSpell, AttackSpellBase

interface DefenceSpellBase : SpellBase {
    val baseHealing: Int
    val baseArmorInc: Int
    val baseResistanceInc: Int
    val baseInjury: Int
}

@Serializable
@SerialName("GrowingDefenceSpell")
data class GrowingDefenceSpell(
    override val id: String,
    override val name: String,
    override val description: String,
    override val picturePath: String,
    override val tick: Int,
    override val spiritCost: Int,
    override val lightFactor: Float,
    override val darkFactor: Float,
    override val baseHealing: Int,
    override val baseArmorInc: Int,
    override val baseResistanceInc: Int,
    override val baseInjury: Int

) : GrowingSpell, DefenceSpellBase

@Serializable
@SerialName("StillDefenseSpell")
data class StillDefenseSpell(
    override val id: String,
    override val name: String,
    override val description: String,
    override val picturePath: String,
    override val tick: Int,
    override val spiritCost: Int,
    override val baseHealing: Int,
    override val baseArmorInc: Int,
    override val baseResistanceInc: Int,
    override val baseInjury: Int

) : StillSpell, DefenceSpellBase

interface ResourceSpellBase : SpellBase {
    val goldIncome: Int
    val spiritIncome: Int
}

@Serializable
@SerialName("StillResourceSpell")
data class StillResourceSpell(
    override val id: String,
    override val name: String,
    override val description: String,
    override val picturePath: String,
    override val tick: Int,
    override val spiritCost: Int,
    override val goldIncome: Int,
    override val spiritIncome: Int
) : ResourceSpellBase, StillSpell

@Serializable
@SerialName("GrowingResourceSpell")
data class GrowingResourceSpell(
    override val id: String,
    override val name: String,
    override val description: String,
    override val picturePath: String,
    override val tick: Int,
    override val spiritCost: Int,
    override val goldIncome: Int,
    override val spiritIncome: Int,
    override val lightFactor: Float,
    override val darkFactor: Float
) : ResourceSpellBase, GrowingSpell

interface InvocatorSpellBase : SpellBase

@Serializable
@SerialName("InvocatorAttackSpell")
data class InvocatorAttackSpell(
    override val id: String,
    override val name: String,
    override val description: String,
    override val picturePath: String,
    override val tick: Int,
    override val spiritCost: Int,
    override val baseDamage: Int,
    override val baseInjury: Int
) : AttackSpellBase, StillSpell, InvocatorSpellBase

@Serializable
@SerialName("InvocatorDefenceSpell")
data class InvocatorDefenceSpell(
    override val id: String,
    override val name: String,
    override val description: String,
    override val picturePath: String,
    override val tick: Int,
    override val spiritCost: Int,
    override val baseHealing: Int,
    override val baseArmorInc: Int,
    override val baseResistanceInc: Int,
    override val baseInjury: Int
) : DefenceSpellBase, StillSpell, InvocatorSpellBase

@Serializable
@SerialName("InvocatorResourceSpell")
data class InvocatorResourceSpell(
    override val id: String,
    override val name: String,
    override val description: String,
    override val picturePath: String,
    override val tick: Int,
    override val spiritCost: Int,
    override val goldIncome: Int,
    override val spiritIncome: Int
) : ResourceSpellBase, InvocatorSpellBase

interface MoveSpellBase : SpellBase

@Serializable
@SerialName("InvocatorMoveSpell")
data class InvocatorMoveSpell(
    override val id: String,
    override val name: String,
    override val description: String,
    override val picturePath: String,
    override val tick: Int,
    override val spiritCost: Int,
    val ignoreDamage: Boolean
) : MoveSpellBase, InvocatorSpellBase

interface PassiveAptitude : CardBase {
    val cooldown: Int
}

@Serializable
@SerialName("ResourcePassiveAptitude")
data class ResourcePassiveAptitude(
    override val id: String,
    override val name: String,
    override val description: String,
    override val picturePath: String,
    override val tick: Int,
    override val spiritCost: Int,
    override val goldIncome: Int,
    override val spiritIncome: Int,
    override val cooldown: Int

) : PassiveAptitude, ResourceSpellBase

interface Invocation : CardBase {
    val maxHP: Int
    val currentHP: Int
    val baseArmor: Int
    val baseResistance: Int
    val baseDamage: Int
}

interface Sorcerer : Invocation {
    val spells: Array<SpellBase>
    val passives: Array<PassiveAptitude>
}

interface Responable

@Serializable
@SerialName("Hero")
data class Hero(
    override val id: String,
    override val name: String,
    override val description: String,
    override val picturePath: String,
    override val maxHP: Int,
    override val currentHP: Int,
    override val baseArmor: Int,
    override val baseResistance: Int,
    override val baseDamage: Int,
    override val spells: Array<SpellBase>,
    override val passives: Array<PassiveAptitude>
) : Sorcerer, Responable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Hero

        if (id != other.id) return false
        if (name != other.name) return false
        if (description != other.description) return false
        if (picturePath != other.picturePath) return false
        if (maxHP != other.maxHP) return false
        if (currentHP != other.currentHP) return false
        if (baseArmor != other.baseArmor) return false
        if (baseResistance != other.baseResistance) return false
        if (baseDamage != other.baseDamage) return false
        if (!spells.contentEquals(other.spells)) return false
        if (!passives.contentEquals(other.passives)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + picturePath.hashCode()
        result = 31 * result + maxHP
        result = 31 * result + currentHP
        result = 31 * result + baseArmor
        result = 31 * result + baseResistance
        result = 31 * result + baseDamage
        result = 31 * result + spells.contentHashCode()
        result = 31 * result + passives.contentHashCode()
        return result
    }
}
