package com.bubul.col.game.ui.elements.cards

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.bubul.col.game.core.game.cards.CardBase
import com.bubul.col.game.core.game.cards.Hero
import com.bubul.col.game.ui.getGameResource
import ktx.scene2d.image
import ktx.scene2d.scene2d
import ktx.scene2d.table
import org.slf4j.LoggerFactory

open class CardView(private val card: CardBase) {

    private var container: Table
    private var miniature: Image

    init {
        container = scene2d.table {

        }
        miniature = scene2d.image(TextureRegionDrawable(TextureRegion(Texture(getGameResource(card.picturePath))))) {
            setSize(50f, 50f)
        }
    }

    fun getUI(): Table {
        return container
    }

    fun getMiniature(): Image {
        return miniature
    }
}

class HeroCardView(private val card: Hero) : CardView(card)

class CardViewFactory {
    companion object {
        private val logger = LoggerFactory.getLogger(CardViewFactory::class.java)

        fun fromList(cards: List<CardBase>?): List<CardView> {
            if (cards == null)
                return listOf()
            val res = mutableListOf<CardView>()
            for (card in cards) {
                when (card) {
                    is Hero -> res.add(fromHeroCard(card))
                    else -> res.add(fromCardBase(card))
                }
            }
            return res
        }

        fun fromCardBase(card: CardBase): CardView {
            logger.info("Create card view for ${card.id}:${card.name}")
            return CardView(card)
        }

        fun fromHeroCard(card: Hero): HeroCardView {
            logger.info("Create hero card view for ${card.id}:${card.name}")
            return HeroCardView(card)
        }
    }
}