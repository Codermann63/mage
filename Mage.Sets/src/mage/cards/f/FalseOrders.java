/*
 *  Copyright 2010 BetaSteward_at_googlemail.com. All rights reserved.
 * 
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 * 
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 * 
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 * 
 *  THIS SOFTWARE IS PROVIDED BY BetaSteward_at_googlemail.com ``AS IS'' AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL BetaSteward_at_googlemail.com OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and should not be interpreted as representing official policies, either expressed
 *  or implied, of BetaSteward_at_googlemail.com.
 */
package mage.cards.f;

import java.util.Set;
import java.util.UUID;
import mage.abilities.Ability;
import mage.abilities.common.CastOnlyDuringPhaseStepSourceAbility;
import mage.abilities.effects.Effect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.RemoveFromCombatTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.PhaseStep;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterAttackingCreature;
import mage.filter.predicate.ObjectPlayer;
import mage.filter.predicate.ObjectPlayerPredicate;
import mage.filter.predicate.mageobject.CardTypePredicate;
import mage.game.Controllable;
import mage.game.Game;
import mage.game.combat.CombatGroup;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.TargetPermanent;
import mage.target.common.TargetAttackingCreature;
import mage.watchers.common.BlockedByOnlyOneCreatureThisCombatWatcher;

/**
 *
 * @author L_J
 */
public class FalseOrders extends CardImpl {

    private static final FilterPermanent filter = new FilterPermanent("creature defending player controls");

    static {
        filter.add(new CardTypePredicate(CardType.CREATURE));
        filter.add(new FalseOrdersDefendingPlayerControlsPredicate());
    }

    public FalseOrders(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{R}");

        // Cast False Orders only during the declare blockers step.
        this.addAbility(new CastOnlyDuringPhaseStepSourceAbility(null, PhaseStep.DECLARE_BLOCKERS, null, "Cast {this} only during the declare blockers step"));

        // Remove target creature defending player controls from combat. Creatures it was blocking that had become blocked by only that creature this combat become unblocked. You may have it block an attacking creature of your choice.
        this.getSpellAbility().addTarget(new TargetPermanent(filter));
        this.getSpellAbility().addEffect(new FalseOrdersUnblockEffect());
        this.getSpellAbility().addWatcher(new BlockedByOnlyOneCreatureThisCombatWatcher());
    }

    public FalseOrders(final FalseOrders card) {
        super(card);
    }

    @Override
    public FalseOrders copy() {
        return new FalseOrders(this);
    }

}

class FalseOrdersDefendingPlayerControlsPredicate implements ObjectPlayerPredicate<ObjectPlayer<Controllable>> {

    @Override
    public boolean apply(ObjectPlayer<Controllable> input, Game game) {
        return game.getCombat().getPlayerDefenders(game).contains(input.getObject().getControllerId());
    }
}

class FalseOrdersUnblockEffect extends OneShotEffect {

    public FalseOrdersUnblockEffect() {
        super(Outcome.Benefit);
        this.staticText = "Remove target creature defending player controls from combat. Creatures it was blocking that had become blocked by only that creature this combat become unblocked. You may have it block an attacking creature of your choice";
    }

    public FalseOrdersUnblockEffect(final FalseOrdersUnblockEffect effect) {
        super(effect);
    }

    @Override
    public FalseOrdersUnblockEffect copy() {
        return new FalseOrdersUnblockEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Permanent permanent = game.getPermanent(source.getTargets().getFirstTarget());
        if (controller != null && permanent != null) {

            // Remove target creature from combat
            Effect effect = new RemoveFromCombatTargetEffect();
            effect.apply(game, source);

            // Make blocked creatures unblocked
            BlockedByOnlyOneCreatureThisCombatWatcher watcher = (BlockedByOnlyOneCreatureThisCombatWatcher) game.getState().getWatchers().get(BlockedByOnlyOneCreatureThisCombatWatcher.class.getSimpleName());
            if (watcher != null) {
                Set<CombatGroup> combatGroups = watcher.getBlockedOnlyByCreature(permanent.getId());
                if (combatGroups != null) {
                    for (CombatGroup combatGroup : combatGroups) {
                        if (combatGroup != null) {
                            combatGroup.setBlocked(false);
                        }
                    }
                }
            }

            // Choose new creature to block
            if (permanent.isCreature()) {
                if (controller.chooseUse(Outcome.Benefit, "Do you want " + permanent.getLogName() + " to block an attacking creature?", source, game)) {
                    TargetAttackingCreature target = new TargetAttackingCreature(1, 1, new FilterAttackingCreature(), true);
                    if (target.canChoose(source.getSourceId(), controller.getId(), game)) {
                        while (!target.isChosen() && target.canChoose(controller.getId(), game) && controller.canRespond()) {
                            controller.chooseTarget(outcome, target, source, game);
                        }
                    } else {
                        return true;
                    }
                    Permanent chosenPermanent = game.getPermanent(target.getFirstTarget());
                    if (chosenPermanent != null && permanent != null && chosenPermanent.isCreature() && controller != null) {
                        CombatGroup chosenGroup = game.getCombat().findGroup(chosenPermanent.getId());
                        if (chosenGroup != null) {
                            // Relevant ruling for Balduvian Warlord:
                            // 7/15/2006 	If an attacking creature has an ability that triggers “When this creature becomes blocked,” 
                            // it triggers when a creature blocks it due to the Warlord’s ability only if it was unblocked at that point.
                            
                            boolean notYetBlocked = true;
                            if (!chosenGroup.getBlockers().isEmpty()) {
                                notYetBlocked = false;
                            }
                            chosenGroup.addBlocker(permanent.getId(), controller.getId(), game);
                            if (notYetBlocked) {
                                game.fireEvent(GameEvent.getEvent(GameEvent.EventType.CREATURE_BLOCKED, chosenPermanent.getId(), null));
                            }
                            game.fireEvent(GameEvent.getEvent(GameEvent.EventType.BLOCKER_DECLARED, chosenPermanent.getId(), permanent.getId(), permanent.getControllerId()));
                        }
                    }
                }    
                return true;   
            }
        }
        return false;
    }
}
