package com.bubul.col.game.ui.elements.cards

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.bubul.col.game.core.game.cards.*
import com.bubul.col.game.ui.utils.TextureBuilder
import ktx.scene2d.image
import ktx.scene2d.label
import ktx.scene2d.scene2d
import ktx.scene2d.table
import org.slf4j.LoggerFactory

open class CardView(private val card: CardBase) {

    protected lateinit var libraryUITable: Table
    private lateinit var miniature: Image
    private lateinit var imageTexture: TextureRegionDrawable

    open fun init() {
        imageTexture = TextureBuilder.getTextureRegionDrawable(card.picturePath)
        libraryUITable = scene2d.table {
            image(imageTexture) {
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
        miniature = scene2d.image(imageTexture) {
            setSize(50f, 50f)
        }
    }

    fun getLibraryUI(): Table {
        return libraryUITable
    }

    fun getMiniatureImage(): Image {
        return miniature
    }

    open fun resetLibraryView() {

    }

    fun getNameLabel(): Label {
        return scene2d.label(card.name, "gold-title").apply {
            setAlignment(Align.center)
            this.fontScaleY = 0.45f
            this.fontScaleX = 0.45f
        }
    }
}

open class InvocationView(private val card: Invocation) : CardView(card) {
    override fun init() {
        super.init()
        libraryUITable.row()
        libraryUITable.add(scene2d.table {

            image(TextureBuilder.getTextureRegionDrawable("icons/icons8-aimer-64.png")) {
                it.size(30f)
            }
            label(card.maxHP.toString(), "gold-title") {
                it.pad(2f)
            }
            image(TextureBuilder.getTextureRegionDrawable("icons/icons8-épée-64.png")) {
                it.size(30f)
            }
            label(card.baseDamage.toString(), "gold-title") {
                it.pad(2f)
            }
            row()
            image(TextureBuilder.getTextureRegionDrawable("icons/icons8-plastron-d'armure-64.png")) {
                it.size(30f)
            }
            label(card.baseArmor.toString(), "gold-title")
            {
                it.pad(2f)
            }
            image(TextureBuilder.getTextureRegionDrawable("icons/icons8-bulle-64.png")) {
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
    private lateinit var spellTable: Table
    private lateinit var spellDetailsContainer: Table
    private var spellDetails: Table = scene2d.table()
    private var selectedSpell: Image? = null
    private var spellSelectionImage: Image? = null
    private lateinit var passiveTable: Table
    private lateinit var passiveDetailsContainer: Table
    private var passiveDetails: Table = scene2d.table()
    private var selectedPassive: Image? = null
    private var passiveSelectionImage: Image? = null

    override fun init() {
        super.init()
        spellTable = scene2d.table {
            table {
                for (spell in card.spells) {
                    table {
                        image((TextureBuilder.getTextureRegionDrawable(spell.picturePath))) {
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

                                        val selectionTable = (selectedSpell!!.parent.getChild(1) as Table)
                                        spellSelectionImage?.remove()
                                        selectionTable.clear()
                                        spellSelectionImage =
                                            Image(TextureBuilder.getColorFilledTextureRegionDrawable(50, 4, Color.GOLD))
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
                        image((TextureBuilder.getTextureRegionDrawable(passive.picturePath))) {
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

                                        val selectionTable = (selectedPassive!!.parent.getChild(1) as Table)
                                        passiveSelectionImage?.remove()
                                        selectionTable.clear()
                                        passiveSelectionImage =
                                            Image(TextureBuilder.getColorFilledTextureRegionDrawable(50, 4, Color.GOLD))
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
    override fun init() {
        super.init()
        libraryUITable.row()
        libraryUITable.add(scene2d.table {

            image(TextureBuilder.getTextureRegionDrawable("icons/icons8-hourglass-64.png")) {
                it.size(30f)
            }
            label(card.tick.toString(), "gold-title") {
                it.pad(2f)
            }
            image(TextureBuilder.getTextureRegionDrawable("icons/icons8-energy-64.png")) {
                it.size(30f)
            }
            label(card.spiritCost.toString(), "gold-title") {
                it.pad(2f)
            }
        })
    }
}

open class HeroSpellView(private val card: SpellBase) : SpellView(card)
open class ResourceSpellView(private val card: ResourceSpellBase) : HeroSpellView(card) {
    override fun init() {
        super.init()
        CardElementBuilder.addRessourcesElement(libraryUITable, card)
    }
}

class HeroStillResourceSpellView(private val card: StillResourceSpell) : ResourceSpellView(card)
class HeroGrowingResourceSpellView(private val card: GrowingResourceSpell) : ResourceSpellView(card) {
    override fun init() {
        super.init()
        CardElementBuilder.addGrowingElement(libraryUITable, card)
    }
}

open class AttackSpellView(private val card: AttackSpellBase) : HeroSpellView(card) {
    override fun init() {
        super.init()
        CardElementBuilder.addAttackElement(libraryUITable, card)
    }
}

class HeroStillAttackSpellView(private val card: StillAttackSpell) : AttackSpellView(card)
class HeroGrowingAttackSpellView(private val card: GrowingAttackSpell) : AttackSpellView(card) {
    override fun init() {
        super.init()
        CardElementBuilder.addGrowingElement(libraryUITable, card)
    }
}

open class DefenceSpellView(private val card: DefenceSpellBase) : HeroSpellView(card) {
    override fun init() {
        super.init()
        CardElementBuilder.addDefenceElement(libraryUITable, card)
    }
}

class HeroStillDefenseSpellView(private val card: StillDefenseSpell) : DefenceSpellView(card)
class HeroGrowingDefenceSpellView(private val card: GrowingDefenceSpell) : DefenceSpellView(card) {
    override fun init() {
        super.init()
        CardElementBuilder.addGrowingElement(libraryUITable, card)
    }
}

open class ReinforcementSpellView(private val card: ReinforcementSpellBase) : HeroSpellView(card) {
    override fun init() {
        super.init()
        CardElementBuilder.addReinforcementElement(libraryUITable, card)
    }
}

class HeroStillReinforcementSpellView(private val card: StillReinforcementSpell) : ReinforcementSpellView(card)
class HeroGrowingReinforcementSpellView(private val card: GrowingReinforcementSpell) : ReinforcementSpellView(card) {
    override fun init() {
        super.init()
        CardElementBuilder.addGrowingElement(libraryUITable, card)
    }
}

open class InvocatorSpellView(private val card: InvocatorSpellBase) : SpellView(card)
open class InvocatorAttackSpellView(private val card: InvocatorAttackSpell) : InvocatorSpellView(card) {
    override fun init() {
        super.init()
        CardElementBuilder.addAttackElement(libraryUITable, card)
    }
}

open class InvocatorDefenceSpellView(private val card: InvocatorDefenceSpell) : InvocatorSpellView(card) {
    override fun init() {
        super.init()
        CardElementBuilder.addDefenceElement(libraryUITable, card)
    }
}

open class InvocatorResourceSpellView(private val card: InvocatorResourceSpell) : InvocatorSpellView(card) {
    override fun init() {
        super.init()
        CardElementBuilder.addRessourcesElement(libraryUITable, card)
    }
}

open class InvocatorMoveSpellView(private val card: InvocatorMoveSpell) : InvocatorSpellView(card)

open class PassiveAptitudeView(private val card: PassiveAptitude) : CardView(card) {
    override fun init() {
        super.init()
        libraryUITable.row()
        libraryUITable.add(scene2d.table {
            image(TextureBuilder.getTextureRegionDrawable("icons/icons8-pause-64.png")) {
                it.size(30f)
            }
            label(card.cooldown.toString(), "gold-title") {
                it.pad(2f)
            }
        })
    }
}

class ResourcePassiveAptitudeView(private val card: ResourcePassiveAptitude) : PassiveAptitudeView(card) {
    override fun init() {
        super.init()
        CardElementBuilder.addRessourcesElement(libraryUITable, card)
    }
}

class ReinforcementPassiveAptitudeView(private val card: ReinforcementPassiveAptitude) : PassiveAptitudeView(card) {
    override fun init() {
        super.init()
        CardElementBuilder.addReinforcementElement(libraryUITable, card)
    }
}

class CardElementBuilder {
    companion object {
        fun addRessourcesElement(libraryUITable: Table, card: ResourceSpellBase) {
            libraryUITable.row()
            libraryUITable.add(scene2d.table {
                image(TextureBuilder.getTextureRegionDrawable("icons/icons8-gold-bars-increase-64.png")) {
                    it.size(30f)
                }
                label(card.goldIncome.toString(), "gold-title") {
                    it.pad(2f)
                }
                image(TextureBuilder.getTextureRegionDrawable("icons/icons8-energy-increase-64.png")) {
                    it.size(30f)
                }
                label(card.spiritIncome.toString(), "gold-title") {
                    it.pad(2f)
                }
            })
        }

        fun addAttackElement(libraryUITable: Table, card: AttackSpellBase) {
            libraryUITable.row()
            libraryUITable.add(scene2d.table {
                image(TextureBuilder.getTextureRegionDrawable("icons/icons8-épée-64.png")) {
                    it.size(30f)
                }
                label(card.baseDamage.toString(), "gold-title") {
                    it.pad(2f)
                }
                image(TextureBuilder.getTextureRegionDrawable("icons/icons8-bandage-64.png")) {
                    it.size(30f)
                }
                label(card.baseInjury.toString(), "gold-title") {
                    it.pad(2f)
                }
            })
        }

        fun addDefenceElement(libraryUITable: Table, card: DefenceSpellBase) {
            libraryUITable.row()
            libraryUITable.add(scene2d.table {
                table {
                    image(TextureBuilder.getTextureRegionDrawable("icons/icons8-bandage-64.png")) {
                        it.size(30f)
                    }
                    label(card.baseInjury.toString(), "gold-title") {
                        it.pad(2f)
                    }
                }
                row()
                table {
                    image(TextureBuilder.getTextureRegionDrawable("icons/icons8-medicine-65.png")) {
                        it.size(30f)
                    }
                    label(card.baseHealing.toString(), "gold-title") {
                        it.pad(2f)
                    }
                    image(TextureBuilder.getTextureRegionDrawable("icons/icons8-plastron-d'armure-64.png")) {
                        it.size(30f)
                    }
                    label(card.baseArmorInc.toString(), "gold-title") {
                        it.pad(2f)
                    }
                    image(TextureBuilder.getTextureRegionDrawable("icons/icons8-bulle-64.png")) {
                        it.size(30f)
                    }
                    label(card.baseResistanceInc.toString(), "gold-title") {
                        it.pad(2f)
                    }
                }
            })
        }

        fun addGrowingElement(libraryUITable: Table, card: GrowingSpell) {
            libraryUITable.row()
            libraryUITable.add(scene2d.table {
                image(TextureBuilder.getTextureRegionDrawable("icons/icons8-ange-64.png")) {
                    it.size(30f)
                }
                label(card.lightFactor.toString(), "gold-title") {
                    it.pad(2f)
                }
                image(TextureBuilder.getTextureRegionDrawable("icons/icons8-diabolique-64.png")) {
                    it.size(30f)
                }
                label(card.darkFactor.toString(), "gold-title") {
                    it.pad(2f)
                }
            })
        }

        fun addReinforcementElement(libraryUITable: Table, card: ReinforcementSpellBase) {
            libraryUITable.row()
            libraryUITable.add(scene2d.table {
                image(TextureBuilder.getTextureRegionDrawable("icons/icons8-aimer-increase-64.png")) {
                    it.size(30f)
                }
                label(card.hpInc.toString(), "gold-title") {
                    it.pad(2f)
                }
                image(TextureBuilder.getTextureRegionDrawable("icons/icons8-shield-64.png")) {
                    it.size(30f)
                }
                label(card.shieldInc.toString(), "gold-title") {
                    it.pad(2f)
                }
                row()
                image(TextureBuilder.getTextureRegionDrawable("icons/icons8-plastron-d'armure-increase-64.png")) {
                    it.size(30f)
                }
                label(card.armorInc.toString(), "gold-title") {
                    it.pad(2f)
                }
                image(TextureBuilder.getTextureRegionDrawable("icons/icons8-bulle-increase-64.png")) {
                    it.size(30f)
                }
                label(card.resistanceInc.toString(), "gold-title") {
                    it.pad(2f)
                }
                row()
                image(TextureBuilder.getTextureRegionDrawable("icons/icons8-épée-increase-64.png")) {
                    it.size(30f)
                }
                label(card.attackInc.toString(), "gold-title") {
                    it.pad(2f)
                }
                image(TextureBuilder.getTextureRegionDrawable("icons/icons8-explosion-increase-64.png")) {
                    it.size(30f)
                }
                label(card.criticsInc.toString(), "gold-title") {
                    it.pad(2f)
                }
            })
        }
    }
}

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
                is PassiveAptitude -> PassiveAptitudeViewFactory.fromAnyPassive(card)
                is SpellBase -> SpellViewFactory.fromAnySpell(card)
                else -> fromCardBase(card)
            }
            view.init()
            cache[card.id] = view
            return view
        }

        private fun fromCardBase(card: CardBase): CardView {
            return CardView(card)
        }
    }
}

private class PassiveAptitudeViewFactory {
    companion object {
        private val logger = LoggerFactory.getLogger(CardViewFactory::class.java)

        fun fromAnyPassive(card: PassiveAptitude): PassiveAptitudeView {
            return when (card) {
                is ResourcePassiveAptitude -> fromResourcePassiveAptitude(card)
                is ReinforcementPassiveAptitude -> fromReinforcementPassiveAptitude(card)
                else -> fromPassiveAptitude(card)
            }
        }

        private fun fromReinforcementPassiveAptitude(card: ReinforcementPassiveAptitude): ReinforcementPassiveAptitudeView {
            logger.info("Using ReinforcementPassiveAptitudeView")
            return ReinforcementPassiveAptitudeView(card)
        }

        private fun fromResourcePassiveAptitude(card: ResourcePassiveAptitude): ResourcePassiveAptitudeView {
            logger.info("Using ResourcePassiveAptitudeView")
            return ResourcePassiveAptitudeView(card)
        }

        private fun fromPassiveAptitude(card: PassiveAptitude): PassiveAptitudeView {
            logger.info("Using PassiveAptitudeView")
            return PassiveAptitudeView(card)
        }
    }
}

private class InvocationViewFactory {
    companion object {
        private val logger = LoggerFactory.getLogger(CardViewFactory::class.java)

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
            logger.info("Using SorcererView")
            return SorcererView(card)
        }

        private fun fromInvocationBase(card: Invocation): InvocationView {
            logger.info("Using InvocationView")
            return InvocationView(card)
        }

        private fun fromHeroCard(card: Hero): HeroCardView {
            logger.info("Using HeroCardView")
            return HeroCardView(card)
        }
    }
}

private class SpellViewFactory {
    companion object {
        private val logger = LoggerFactory.getLogger(CardViewFactory::class.java)

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
            logger.info("Using InvocatorMoveSpellView")
            return InvocatorMoveSpellView(card)
        }

        private fun fromInvocatorStillResourceSpell(card: InvocatorResourceSpell): InvocatorResourceSpellView {
            logger.info("Using InvocatorResourceSpellView")
            return InvocatorResourceSpellView(card)
        }

        private fun fromInvocatorStillDefenceSpell(card: InvocatorDefenceSpell): InvocatorDefenceSpellView {
            logger.info("Using InvocatorDefenceSpellView")
            return InvocatorDefenceSpellView(card)
        }

        private fun fromInvocatorStillAttackSpell(card: InvocatorAttackSpell): InvocatorAttackSpellView {
            logger.info("Using InvocatorAttackSpellView")
            return InvocatorAttackSpellView(card)
        }

        private fun fromAnyHeroStillSpell(card: StillSpell): SpellView {
            return when (card) {
                is AttackSpellBase -> fromHeroStillAttackSpell(card as StillAttackSpell)
                is DefenceSpellBase -> fromHeroStillDefenceSpell(card as StillDefenseSpell)
                is ResourceSpellBase -> fromHeroStillResourceSpell(card as StillResourceSpell)
                is ReinforcementSpellBase -> fromHeroStillReinforcementSpell(card as StillReinforcementSpell)
                else -> fromSpellBase(card)
            }
        }

        private fun fromHeroStillReinforcementSpell(card: StillReinforcementSpell): HeroStillReinforcementSpellView {
            logger.info("Using HeroStillReinforcementSpellView")
            return HeroStillReinforcementSpellView(card)
        }

        private fun fromHeroStillResourceSpell(card: StillResourceSpell): HeroStillResourceSpellView {
            logger.info("Using HeroStillResourceSpellView")
            return HeroStillResourceSpellView(card)
        }

        private fun fromHeroStillDefenceSpell(card: StillDefenseSpell): HeroStillDefenseSpellView {
            logger.info("Using HeroStillDefenseSpellView")
            return HeroStillDefenseSpellView(card)
        }

        private fun fromHeroStillAttackSpell(card: StillAttackSpell): HeroStillAttackSpellView {
            logger.info("Using HeroStillAttackSpellView")
            return HeroStillAttackSpellView(card)
        }

        private fun fromAnyHeroGrowingSpell(card: GrowingSpell): SpellView {
            return when (card) {
                is AttackSpellBase -> fromHeroGrowingAttackSpell(card as GrowingAttackSpell)
                is DefenceSpellBase -> fromHeroGrowingDefenceSpell(card as GrowingDefenceSpell)
                is ResourceSpellBase -> fromHeroGrowingResourceSpell(card as GrowingResourceSpell)
                is ReinforcementSpellBase -> fromHeroGrowingReinforcementSpell(card as GrowingReinforcementSpell)
                else -> fromSpellBase(card)
            }
        }

        private fun fromHeroGrowingReinforcementSpell(card: GrowingReinforcementSpell): HeroGrowingReinforcementSpellView {
            logger.info("Using HeroGrowingReinforcementSpellView")
            return HeroGrowingReinforcementSpellView(card)
        }

        private fun fromHeroGrowingResourceSpell(card: GrowingResourceSpell): HeroGrowingResourceSpellView {
            logger.info("Using HeroGrowingResourceSpellView")
            return HeroGrowingResourceSpellView(card)
        }

        private fun fromHeroGrowingDefenceSpell(card: GrowingDefenceSpell): HeroGrowingDefenceSpellView {
            logger.info("Using HeroGrowingDefenceSpellView")
            return HeroGrowingDefenceSpellView(card)
        }

        private fun fromHeroGrowingAttackSpell(card: GrowingAttackSpell): HeroGrowingAttackSpellView {
            logger.info("Using HeroGrowingAttackSpellView")
            return HeroGrowingAttackSpellView(card)
        }

        private fun fromSpellBase(card: SpellBase): SpellView {
            logger.info("Using SpellView")
            return SpellView(card)
        }
    }
}