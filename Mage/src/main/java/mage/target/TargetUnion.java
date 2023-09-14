package mage.target;


import mage.MageObject;
import mage.abilities.Ability;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.filter.Filter;
import mage.filter.FilterCard;
import mage.game.Game;
import mage.players.Player;

import java.util.*;

public class TargetUnion extends TargetImpl {
    ArrayList<Target> targets = new ArrayList<>();
    private FilterCard filter;

    public TargetUnion(Target... argtargets) {
        Collections.addAll(targets, argtargets);
    }
    @Override
    public boolean canChoose(UUID sourceControllerId, Ability source, Game game) {
        for (Target t: targets){
            if (t.canChoose(sourceControllerId, source, game)){return true;}
        }
        return false;
    }

    @Override
    public Set<UUID> possibleTargets(UUID sourceControllerId, Ability source, Game game) {
        Set<UUID> set = new HashSet<UUID>();
        for (Target t: targets){
            set.addAll(t.possibleTargets(sourceControllerId, source, game));
        }
        return set;
    }

    @Override
    public boolean canTarget(UUID id, Game game) {
        for (Target t: targets){
            if (t.canTarget(id, game)){return true;}
        }
        return false;
    }

    @Override
    public boolean canTarget(UUID id, Ability source, Game game) {
        for (Target t: targets){
            if (t.canTarget(id, source, game)){return true;}
        }
        return false;
    }

    @Override
    public boolean canTarget(UUID playerId, UUID id, Ability source, Game game) {
        for (Target t: targets){
            if (t.canTarget(playerId, id, source, game)){return true;}
        }
        return false;
    }

    @Override
    public boolean canChoose(UUID sourceControllerId, Game game) {
        for (Target t: targets){
            if (t.canChoose(sourceControllerId, game)){return true;}
        }
        return false;
    }

    @Override
    public Set<UUID> possibleTargets(UUID sourceControllerId, Game game) {
        Set<UUID> set = new HashSet<UUID>();
        for (Target t: targets){
            set.addAll(t.possibleTargets(sourceControllerId, game));
        }
        return set;
    }

    @Override
    public String getTargetedName(Game game) {
        StringBuilder sb = new StringBuilder();
        for (UUID targetId : getTargets()) {
            MageObject object = game.getObject(targetId);
            if (object != null) {
                sb.append(object.getLogName()).append(' ');
            }
        }
        return sb.toString().trim();
    }

    @Override
    public FilterCard getFilter() {
        return this.filter;
    }
    

    @Override
    public TargetImpl copy() {
        return new TargetUnion();
    }
}
