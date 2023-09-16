package mage.target.common;

import mage.MageItem;
import mage.abilities.Ability;
import mage.cards.Card;
import mage.cards.Cards;
import mage.constants.CommanderCardType;
import mage.constants.Zone;
import mage.filter.FilterCard;
import mage.game.Game;
import mage.game.events.TargetEvent;
import mage.players.Player;
import mage.target.Target;
import mage.target.TargetCard;

import java.util.*;
import java.util.stream.Collectors;

public class TargetCardInYourHandOrGraveyard extends TargetCard {

    private ArrayList<TargetCard> multiZone;
    public TargetCardInYourHandOrGraveyard(int minTargets, int maxTargets, FilterCard filter) {
        super(minTargets, maxTargets, Zone.ALL, filter);
        multiZone = new ArrayList<>();
        multiZone.add(new TargetCardInYourGraveyard(filter));
        multiZone.add(new TargetCardInHand(filter));
    }

    /**
     * Checks if there are enough {@link Card cards} in the appropriate zone that the player can choose from among them
     * or if they are autochosen since there are fewer than the minimum number.
     *
     * @param sourceControllerId - controller of the target event source
     * @param source
     * @param game
     * @return - true if enough valid {@link Card} exist
     */
    @Override
    public boolean canChoose(UUID sourceControllerId, Ability source, Game game) {
        UUID sourceId = source != null ? source.getSourceId() : null;
        int possibleTargets = 0;
        if (getNumberOfTargets() == 0) { // if 0 target is valid, the canChoose is always true
            return true;
        }
        Player player = game.getPlayer(sourceControllerId);
        if (player != null) {
            if (this.minNumberOfTargets == 0) {
                return true;
            }
            // possible targets in hand
            for (Card card : player.getHand().getCards(filter, sourceControllerId, source, game)) {
                if (sourceId == null || isNotTarget() || !game.replaceEvent(new TargetEvent(card, sourceId, sourceControllerId))) {
                    possibleTargets++;
                    if (possibleTargets >= this.minNumberOfTargets) {
                        return true;
                    }
                }
            }
            // possible targets in graveyard
            for (Card card : player.getGraveyard().getCards(filter, sourceControllerId, source, game)) {
                if (sourceId == null || isNotTarget() || !game.replaceEvent(new TargetEvent(card, sourceId, sourceControllerId))) {
                    possibleTargets++;
                    if (possibleTargets >= this.minNumberOfTargets) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks if there are enough {@link Card} that can be selected.
     *
     * @param sourceControllerId - controller of the select event
     * @param game
     * @return - true if enough valid {@link Card} exist
     */
    @Override
    public boolean canChoose(UUID sourceControllerId, Game game) {
        return canChoose(sourceControllerId, null, game);
    }

    @Override
    public Set<UUID> possibleTargets(UUID sourceControllerId, Ability source, Game game) {
        Set<UUID> possibleTargets = new HashSet<>();
        for (TargetCard targetZone: multiZone){
            possibleTargets.addAll(targetZone.possibleTargets(sourceControllerId, source, game));
        }

        return possibleTargets;
    }

    public Set<UUID> possibleTargets(UUID sourceControllerId, Cards cards, Ability source, Game game) {
        return cards.getCards(filter, sourceControllerId, source, game).stream().map(MageItem::getId).collect(Collectors.toSet());
    }

    @Override
    public Set<UUID> possibleTargets(UUID sourceControllerId, Game game) {
        return possibleTargets(sourceControllerId, (Ability) null, game);
    }


    @Override
    public boolean canTarget(UUID id, Game game) {
        Card card = game.getCard(id);
        return card != null
                && zone != null && (Zone.GRAVEYARD.match(game.getState().getZone(id)) || Zone.HAND.match(game.getState().getZone(id)))
                && getFilter() != null && getFilter().match(card, game);
    }

    @Override
    public boolean canTarget(UUID id, Ability source, Game game) {
        return canTarget(id, game);
    }

    @Override
    public boolean canTarget(UUID playerId, UUID id, Ability source, Game game) {
        Card card = game.getCard(id);
        return card != null
                && zone != null && (Zone.GRAVEYARD.match(game.getState().getZone(id)) || Zone.HAND.match(game.getState().getZone(id)))
                && getFilter() != null && getFilter().match(card, playerId, source, game);
    }

    public boolean canTarget(UUID playerId, UUID id, Ability source, Cards cards, Game game) {
        return cards.contains(id) && canTarget(playerId, id, source, game);
    }

}
