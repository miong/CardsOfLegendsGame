package com.bubul.col.game.core.game.cards


class CardManager {

    private var heroCardsHolder = mutableMapOf<CardType, Array<CardBase>>()

    fun init(): Boolean {
        val loader = CardSerialization()
        val cards = loader.loadCards()
        if (cards.isEmpty())
            return false
        for (type in CardType.values())
            heroCardsHolder[type] = arrayOf()
        for (card in cards) {
            if (card is Hero) {
                val hero = card as Hero
                heroCardsHolder[CardType.Hero]?.plus(hero)
                for (subcard in hero.spells)
                    heroCardsHolder[CardType.HeroSpell]?.plus(subcard)
                for (subcard in hero.passives)
                    heroCardsHolder[CardType.HeroPassive]?.plus(card)
            } else if (card is SpellBase) {
                //InvocatorSpell
                heroCardsHolder[CardType.InvocatorSpell]?.plus(card)
            } else {
                //InvocatorPassives
                heroCardsHolder[CardType.InvocatorPassive]?.plus(card)
            }
        }
        return true

    }

    fun dispose() {

    }
}