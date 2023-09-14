package mage.target;

import mage.cards.Cards;
import mage.constants.Zone;

public class TargetCardUnion extends TargetCard {

    public TargetCardUnion(Cards cardsInHand, Cards cardsInGraveyard) {
        super(Zone.ALL);
    }
}
