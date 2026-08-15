package dev.fabbrini.armorset.listeners;

import dev.fabbrini.armorset.items.ArmorItems;
import dev.fabbrini.armorset.items.ArmorPiece;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Collection;

/**
 * Centralizes the two fall-damage abilities (Suit of Lies + Boots of the Dead Souls)
 * in one handler so they never fight over the same event. If the wearer has Suit of
 * Lies and someone is nearby, the fall damage is negated and transferred to them. If
 * nobody is nearby (or only the boots are worn), fall back to the boots' reduction.
 */
public final class FallDamageListener implements Listener {

    private static final double TRANSFER_RADIUS = 4.0;
    private static final double BOOTS_REDUCTION = 0.7;

    private final ArmorItems armorItems;

    public FallDamageListener(ArmorItems armorItems) {
        this.armorItems = armorItems;
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (armorItems.isWearing(player, ArmorPiece.SUIT_OF_LIES)) {
            Collection<Player> nearby = player.getLocation()
                    .getNearbyPlayers(TRANSFER_RADIUS, other -> other != player);
            if (!nearby.isEmpty()) {
                double transferred = event.getDamage();
                event.setDamage(0);
                for (Player target : nearby) {
                    target.damage(transferred, player);
                }
                return;
            }
        }

        if (armorItems.isWearing(player, ArmorPiece.BOOTS_OF_DEAD_SOULS)) {
            event.setDamage(event.getDamage() * BOOTS_REDUCTION);
        }
    }
}
