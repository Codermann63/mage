
package mage.game.turn;

import java.util.UUID;

import mage.constants.PhaseStep;
import mage.game.Game;
import mage.game.combat.CombatGroup;
import mage.game.events.GameEvent;
import mage.game.events.GameEvent.EventType;

/**
 * @author BetaSteward_at_googlemail.com
 */
public class CombatDamageStep extends Step {

    public CombatDamageStep() {
        super(PhaseStep.COMBAT_DAMAGE, true);
        this.stepEvent = EventType.COMBAT_DAMAGE_STEP;
        this.preStepEvent = EventType.COMBAT_DAMAGE_STEP_PRE;
        this.postStepEvent = EventType.COMBAT_DAMAGE_STEP_POST;
    }

    protected CombatDamageStep(final CombatDamageStep step) {
        super(step);
    }

    @Override
    public void priority(Game game, UUID activePlayerId, boolean resuming) {
        game.fireEvent(new GameEvent(GameEvent.EventType.COMBAT_DAMAGE_STEP_PRIORITY, null, null, activePlayerId));
        super.priority(game, activePlayerId, resuming);
    }

    @Override
    public boolean skipStep(Game game, UUID activePlayerId) {
        if (game.getCombat().noAttackers()) {
            return true;
        }
        return super.skipStep(game, activePlayerId);
    }

    @Override
    public void beginStep(Game game, UUID activePlayerId) {
        super.beginStep(game, activePlayerId);
        for (CombatGroup group : game.getCombat().getGroups()) {
            group.assignDamageToBlockers(false, game);
        }
        for (CombatGroup group : game.getCombat().getBlockingGroups()) {
            group.assignDamageToAttackers(false, game);
        }

        for (CombatGroup group : game.getCombat().getGroups()) {
            group.applyDamage(game);
        }

        for (CombatGroup group : game.getCombat().getBlockingGroups()) {
            group.applyDamage(game);
        }
        // Must fire damage batch events now, before SBA (https://github.com/magefree/mage/issues/9129)
        game.getState().handleSimultaneousEvent(game);
    }

    public boolean getFirst() {
        return false;
    }

    @Override
    public CombatDamageStep copy() {
        return new CombatDamageStep(this);
    }

}
