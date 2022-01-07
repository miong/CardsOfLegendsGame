package com.bubul.col.game.ui.elements.cards

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.bubul.col.game.core.game.cards.*

import com.bubul.col.game.ui.getGameResource
import ktx.scene2d.image
import ktx.scene2d.label
import ktx.scene2d.scene2d
import ktx.scene2d.table
import org.slf4j.LoggerFactory

open class CardView(private val card: CardBase) {

    protected var libraryUITable: Table = scene2d.table {
        image(TextureRegionDrawable(TextureRegion(Texture(getGameResource(card.picturePath))))) {
            it.size(120f)
        }
        row()
        label(card.name, "gold-title") {
            it.padBottom(10f)
        }
        row()
        label(card.description, "gold-title") {
            it.padBottom(10f)
        }
    }
    protected var miniature: Image =
        scene2d.image(TextureRegionDrawable(TextureRegion(Texture(getGameResource(card.picturePath))))) {
            setSize(50f, 50f)
        }

    fun getLibraryUI(): Table {
        return libraryUITable
    }

    fun getMiniatureImage(): Image {
        return miniature
    }

    open fun resetLibraryView() {

    }
}

open class InvocationView(private val card: Invocation) : CardView(card) {
    init {
        libraryUITable.row()
        libraryUITable.add(scene2d.table {
            //TODO use icons
            image(TextureRegionDrawable(TextureRegion(Texture(getGameResource("icons/icons8-aimer-64.png"))))) {
                it.size(30f)
            }
            label(card.maxHP.toString(), "gold-title") {
                it.pad(2f)
            }
            image(TextureRegionDrawable(TextureRegion(Texture(getGameResource("icons/icons8-épée-64.png"))))) {
                it.size(30f)
            }
            label(card.baseDamage.toString(), "gold-title") {
                it.pad(2f)
            }
            row()
            image(TextureRegionDrawable(TextureRegion(Texture(getGameResource("icons/icons8-shield-64.png"))))) {
                it.size(30f)
            }
            label(card.baseArmor.toString(), "gold-title")
            {
                it.pad(2f)
            }
            image(TextureRegionDrawable(TextureRegion(Texture(getGameResource("icons/icons8-bulle-64.png"))))) {
                it.size(30f)
            }
            label(card.baseResistance.toString(), "gold-title")
            {
                it.pad(2f)
            }
        })
    }
}

open class SorcererView(private val card: Sorcerer) : InvocationView(card) {
    private val spellTable: Table
    private lateinit var spellDetailsContainer: Table
    private var spellDetails: Table = scene2d.table()
    private var selectedSpell: Image? = null
    private var spellSelectionImage: Image? = null
    private var passiveTable: Table
    private lateinit var passiveDetailsContainer: Table
    private var passiveDetails: Table = scene2d.table()
    private var selectedPassive: Image? = null
    private var passiveSelectionImage: Image? = null

    init {
        spellTable = scene2d.table {
            table {
                for (spell in card.spells) {
                    table {
                        image((TextureRegionDrawable(TextureRegion(Texture(getGameResource(spell.picturePath)))))) {
                            it.size(50f).pad(5f)
                            touchable = Touchable.enabled
                            addListener(object : ClickListener() {
                                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                                    if (selectedSpell == this@image) {
                                        hideSpellDetail()
                                        selectedSpell = null
                                        spellSelectionImage!!.remove()
                                        spellSelectionImage = null
                                    } else {
                                        selectedSpell = this@image
                                        spellDetailsContainer.clear()
                                        spellDetails = CardViewFactory.fromAnyCard(spell).getLibraryUI()

                                        val yellowHBandPixmap = Pixmap(50, 4, Pixmap.Format.RGBA8888)
                                        yellowHBandPixmap.setColor(Color.GOLD)
                                        yellowHBandPixmap.fill()
                                        val selectionTable = (selectedSpell!!.parent.getChild(1) as Table)
                                        spellSelectionImage?.remove()
                                        selectionTable.clear()
                                        spellSelectionImage =
                                            Image(TextureRegionDrawable(TextureRegion(Texture(yellowHBandPixmap))))
                                        selectionTable.add(spellSelectionImage)
                                        showSpellDetail()
                                    }
                                }
                            })
                        }
                        row()
                        table { }
                    }
                }
            }
            row()
            spellDetailsContainer = table {}
        }
        passiveTable = scene2d.table {
            table {
                for (passive in card.passives) {
                    table {
                        image((TextureRegionDrawable(TextureRegion(Texture(getGameResource(passive.picturePath)))))) {
                            it.size(50f).pad(5f)
                            touchable = Touchable.enabled
                            addListener(object : ClickListener() {
                                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                                    if (selectedPassive == this@image) {
                                        hidePassiveDetail()
                                        selectedPassive = null
                                        passiveSelectionImage!!.remove()
                                        passiveSelectionImage = null
                                    } else {
                                        selectedPassive = this@image
                                        passiveDetailsContainer.clear()
                                        passiveDetails = CardViewFactory.fromAnyCard(passive).getLibraryUI()

                                        val yellowHBandPixmap = Pixmap(50, 4, Pixmap.Format.RGBA8888)
                                        yellowHBandPixmap.setColor(Color.GOLD)
                                        yellowHBandPixmap.fill()
                                        val selectionTable = (selectedPassive!!.parent.getChild(1) as Table)
                                        passiveSelectionImage?.remove()
                                        selectionTable.clear()
                                        passiveSelectionImage =
                                            Image(TextureRegionDrawable(TextureRegion(Texture(yellowHBandPixmap))))
                                        selectionTable.add(passiveSelectionImage)
                                        showPassiveDetail()
                                    }
                                }
                            })
                        }
                        row()
                        table { }
                    }
                }
            }
            row()
            passiveDetailsContainer = table {}
        }
        libraryUITable.row()
        libraryUITable.add(scene2d {
            label("Spells", "gold-title")
        }).row()
        libraryUITable.add(spellTable).row()
        libraryUITable.add(scene2d {
            label("Passives", "gold-title")
        }).row()
        libraryUITable.add(passiveTable)
    }

    private fun showSpellDetail() {
        spellDetailsContainer.add(spellDetails)
    }

    private fun hideSpellDetail() {
        spellDetailsContainer.clear()
    }

    private fun showPassiveDetail() {
        passiveDetailsContainer.add(passiveDetails)
    }

    private fun hidePassiveDetail() {
        passiveDetailsContainer.clear()
    }

    override fun resetLibraryView() {
        hideSpellDetail()
        selectedSpell = null
        spellSelectionImage?.remove()
        spellSelectionImage = null
        hidePassiveDetail()
        selectedPassive = null
        passiveSelectionImage?.remove()
        passiveSelectionImage = null
    }
}

class HeroCardView(private val card: Hero) : SorcererView(card)

open class SpellView(private val card: SpellBase) : CardView(card) {
    init {
        libraryUITable.row()
        libraryUITable.add(scene2d.table {
            //TODO use icons
            image(TextureRegionDrawable(TextureRegion(Texture(getGameResource("icons/icons8-hourglass-64.png"))))) {
                it.size(30f)
            }
            label(card.tick.toString(), "gold-title") {
                it.pad(2f)
            }
            image(TextureRegionDrawable(TextureRegion(Texture(getGameResource("icons/icons8-energy-64.png"))))) {
                it.size(30f)
            }
            label(card.spiritCost.toString(), "gold-title") {
                it.pad(2f)
            }
        })
    }
}

open class HeroSpellView(private val card: SpellBase) : SpellView(card)
open class ResourceSpellView(private val card: ResourceSpellBase) : HeroSpellView(card)
class HeroStillResourceSpellView(private val card: StillResourceSpell) : ResourceSpellView(card)
class HeroGrowingResourceSpellView(private val card: GrowingResourceSpell) : ResourceSpellView(card)
open class AttackSpellView(private val card: AttackSpellBase) : HeroSpellView(card)
class HeroStillAttackSpellView(private val card: StillAttackSpell) : AttackSpellView(card)
class HeroGrowingAttackSpellView(private val card: GrowingAttackSpell) : AttackSpellView(card)
open class DefenceSpellView(private val card: DefenceSpellBase) : HeroSpellView(card)
class HeroStillDefenseSpellView(private val card: StillDefenseSpell) : DefenceSpellView(card)
class HeroGrowingDefenceSpellView(private val card: GrowingDefenceSpell) : DefenceSpellView(card)

open class InvocatorSpellView(private val card: InvocatorSpellBase) : SpellView(card)
open class InvocatorAttackSpellView(private val card: InvocatorAttackSpell) : InvocatorSpellView(card)
open class InvocatorDefenceSpellView(private val card: InvocatorDefenceSpell) : InvocatorSpellView(card)
open class InvocatorResourceSpellView(private val card: InvocatorResourceSpell) : InvocatorSpellView(card)
open class InvocatorMoveSpellView(private val card: InvocatorMoveSpell) : InvocatorSpellView(card)

open class PassiveAptitudeView(private val card: PassiveAptitude) : CardView(card)
class ResourcePassiveAptitudeView(private val card: ResourcePassiveAptitude) : PassiveAptitudeView(card)


class CardViewFactory {
    companion object {
        private val logger = LoggerFactory.getLogger(CardViewFactory::class.java)
        private val cache = mutableMapOf<String, CardView>()

        fun fromList(cards: List<CardBase>?): List<CardView> {
            if (cards == null)
                return listOf()
            val res = mutableListOf<CardView>()
            for (card in cards) {
                res.add(fromAnyCard(card))
            }
            return res
        }

        fun fromAnyCard(card: CardBase): CardView {
            cache[card.id]?.let {
                logger.info("Reuse cached card view for ${card.id}:${card.name}")
                return it
            }
            logger.info("Create card view for ${card.id}:${card.name}")
            val view = when (card) {
                is Invocation -> InvocationViewFactory.fromAnyInvocationCard(card)
                is SpellBase -> SpellViewFactory.fromAnySpell(card)
                is PassiveAptitude -> PassiveAptitudeViewFactory.fromAnyPassive(card)
                else -> fromCardBase(card)
            }
            cache[card.id] = view
            return view
        }

        private fun fromCardBase(card: CardBase): CardView {
            return CardView(card)
        }
    }
}

class PassiveAptitudeViewFactory {
    companion object {
        fun fromAnyPassive(card: PassiveAptitude): PassiveAptitudeView {
            return when (card) {
                is ResourcePassiveAptitude -> fromResourcePassiveAptitude(card)
                else -> fromPassiveAptitude(card)
            }
        }

        private fun fromResourcePassiveAptitude(card: ResourcePassiveAptitude): PassiveAptitudeView {
            return ResourcePassiveAptitudeView(card)
        }

        private fun fromPassiveAptitude(card: PassiveAptitude): PassiveAptitudeView {
            return PassiveAptitudeView(card)
        }
    }
}

class InvocationViewFactory {
    companion object {
        fun fromAnyInvocationCard(card: Invocation): InvocationView {
            return when (card) {
                is Sorcerer -> fromAnySorcererCard(card)
                else -> fromInvocationBase(card)
            }
        }

        private fun fromAnySorcererCard(card: Sorcerer): SorcererView {
            return when (card) {
                is Hero -> fromHeroCard(card)
                else -> fromSorcererCard(card)
            }
        }

        private fun fromSorcererCard(card: Sorcerer): SorcererView {
            return SorcererView(card)
        }

        private fun fromInvocationBase(card: Invocation): InvocationView {
            return InvocationView(card)
        }

        private fun fromHeroCard(card: Hero): HeroCardView {
            return HeroCardView(card)
        }
    }
}

class SpellViewFactory {
    companion object {
        fun fromAnySpell(card: SpellBase): SpellView {
            return when (card) {
                is InvocatorSpellBase -> fromAnyInvocatorSpell(card)
                is GrowingSpell -> fromAnyHeroGrowingSpell(card)
                is StillSpell -> fromAnyHeroStillSpell(card)
                else -> fromSpellBase(card)
            }
        }

        private fun fromAnyInvocatorSpell(card: InvocatorSpellBase): SpellView {
            return when (card) {
                is AttackSpellBase -> fromInvocatorStillAttackSpell(card as InvocatorAttackSpell)
                is DefenceSpellBase -> fromInvocatorStillDefenceSpell(card as InvocatorDefenceSpell)
                is ResourceSpellBase -> fromInvocatorStillResourceSpell(card as InvocatorResourceSpell)
                is MoveSpellBase -> fromInvocatorStillMoveSpell(card as InvocatorMoveSpell)
                else -> fromSpellBase(card)
            }
        }

        private fun fromInvocatorStillMoveSpell(card: InvocatorMoveSpell): InvocatorMoveSpellView {
            return InvocatorMoveSpellView(card)
        }

        private fun fromInvocatorStillResourceSpell(card: InvocatorResourceSpell): InvocatorResourceSpellView {
            return InvocatorResourceSpellView(card)
        }

        private fun fromInvocatorStillDefenceSpell(card: InvocatorDefenceSpell): InvocatorDefenceSpellView {
            return InvocatorDefenceSpellView(card)
        }

        private fun fromInvocatorStillAttackSpell(card: InvocatorAttackSpell): InvocatorAttackSpellView {
            return InvocatorAttackSpellView(card)
        }

        private fun fromAnyHeroStillSpell(card: StillSpell): SpellView {
            return when (card) {
                is AttackSpellBase -> fromHeroStillAttackSpell(card as StillAttackSpell)
                is DefenceSpellBase -> fromHeroStillDefenceSpell(card as StillDefenseSpell)
                is ResourceSpellBase -> fromHeroStillResourceSpell(card as StillResourceSpell)
                else -> fromSpellBase(card)
            }
        }

        private fun fromHeroStillResourceSpell(card: StillResourceSpell): HeroStillResourceSpellView {
            return HeroStillResourceSpellView(card)
        }

        private fun fromHeroStillDefenceSpell(card: StillDefenseSpell): HeroStillDefenseSpellView {
            return HeroStillDefenseSpellView(card)
        }

        private fun fromHeroStillAttackSpell(card: StillAttackSpell): HeroStillAttackSpellView {
            return HeroStillAttackSpellView(card)
        }

        private fun fromAnyHeroGrowingSpell(card: GrowingSpell): SpellView {
            return when (card) {
                is AttackSpellBase -> fromHeroGrowingAttackSpell(card as GrowingAttackSpell)
                is DefenceSpellBase -> fromHeroGrowingDefenceSpell(card as GrowingDefenceSpell)
                is ResourceSpellBase -> fromHeroGrowingResourceSpell(card as GrowingResourceSpell)
                else -> fromSpellBase(card)
            }
        }

        private fun fromHeroGrowingResourceSpell(card: GrowingResourceSpell): HeroGrowingResourceSpellView {
            return HeroGrowingResourceSpellView(card)
        }

        private fun fromHeroGrowingDefenceSpell(card: GrowingDefenceSpell): HeroGrowingDefenceSpellView {
            return HeroGrowingDefenceSpellView(card)
        }

        private fun fromHeroGrowingAttackSpell(card: GrowingAttackSpell): HeroGrowingAttackSpellView {
            return HeroGrowingAttackSpellView(card)
        }

        private fun fromSpellBase(card: SpellBase): SpellView {
            return SpellView(card)
        }
    }
}