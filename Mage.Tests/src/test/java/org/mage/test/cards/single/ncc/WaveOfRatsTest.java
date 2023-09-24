package org.mage.test.cards.single.ncc;

import mage.abilities.keyword.IndestructibleAbility;
import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;
/**
 * @author Codermann63
 */


/**
 * Wave of Rats
 * {3}{B}
 *
 * Creature — Rat
 *
 * Trample
 * When Wave of Rats dies, if it dealt combat damage to a player this turn, return it to the battlefield under its owner’s control.
 * Blitz {4}{B} (If you cast this spell for its blitz cost, it gains haste and "When this creature dies, draw a card." Sacrifice it at the beginning of the next end step.)
 */
public class WaveOfRatsTest extends CardTestPlayerBase {
    final static String WAVEOFRATS = "Wave of Rats";
    final static String MURDER = "Murder";
    @Test
    public void returnAfterKilledAfterAttacking() {
        addCard(Zone.HAND, playerA, MURDER);
        addCard(Zone.BATTLEFIELD, playerA, WAVEOFRATS);
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 4);
        addCard(Zone.LIBRARY, playerA, "Swamp", 4);
        addCard(Zone.LIBRARY, playerB, "Swamp", 4);

        setStrictChooseMode(true);

        attack(1, playerA, WAVEOFRATS, playerB);
        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerA, MURDER, WAVEOFRATS);
        waitStackResolved(1, PhaseStep.POSTCOMBAT_MAIN);

        execute();

        assertPermanentCount(playerA, WAVEOFRATS,1);
        assertGraveyardCount(playerA, WAVEOFRATS,0);
    }


}
