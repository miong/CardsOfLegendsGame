package com.bubul.col.game.core.game.cards

import org.slf4j.LoggerFactory


class CardManager {

    private val logger = LoggerFactory.getLogger(this.javaClass.name)
    private var heroCardsHolder = mutableMapOf<CardType, MutableList<CardBase>>()

    fun init(): Boolean {
        val loader = CardSerialization()
        val cards = loader.loadCards()
        if (cards.isEmpty()) {
            logger.info("Cards loading failed")
            return false
        }
        logger.info("Cards loaded")
        for (type in CardType.values())
            heroCardsHolder[type] = mutableListOf()
        for (card in cards) {
            when (card) {
                is Hero -> {
                    heroCardsHolder[CardType.Hero]?.add(card)
                    for (subcard in card.spells)
                        heroCardsHolder[CardType.HeroSpell]?.add(subcard)
                    for (subcard in card.passives)
                        heroCardsHolder[CardType.HeroPassive]?.add(card)
                }
                is SpellBase -> {
                    //InvocatorSpell
                    heroCardsHolder[CardType.InvocatorSpell]?.add(card)
                }
                else -> {
                    //InvocatorPassives
                    heroCardsHolder[CardType.InvocatorPassive]?.add(card)
                }
            }
        }
        return true

    }

    fun getCards(): Map<CardType, List<CardBase>> {
        return heroCardsHolder;
    }

    fun dispose() {

    }
}