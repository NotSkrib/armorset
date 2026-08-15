package dev.fabbrini.armorset.listeners;

import dev.fabbrini.armorset.items.ArmorItems;
import dev.fabbrini.armorset.items.ArmorPiece;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Ghost of Sparta's damage math. Note this composes with vanilla armor/enchantment
 * reduction (already applied by the time any plugin sees the event) as a simple
 * multiplier on top - an intentional approximation for a single-plugin server.
 */
public final class CombatListener implements Listener {

    private static final double INCOMING_REDUCTION = 0.8;
    private static final double BERSERK_HEALTH_THRESHOLD = 6.0;
    private static final double BERSERK_MULTIPLIER = 1.4;

    private final ArmorItems armorItems;

    public CombatListener(ArmorItems armorItems) {
        this.armorItems = armorItems;
    }

    @EventHandler
    public void onIncomingDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (armorItems.isWearing(player, ArmorPiece.GHOST_OF_SPARTA)) {
            event.setDamage(event.getDamage() * INCOMING_REDUCTION);
        }
    }

    @EventHandler
    public void onOutgoingDamage(EntityDamageByEntityEvent event) {
        Player attacker = resolveAttacker(event.getDamager());
        if (attacker == null || !armorItems.isWearing(attacker, ArmorPiece.GHOST_OF_SPARTA)) {
            return;
        }
        if (attacker.getHealth() < BERSERK_HEALTH_THRESHOLD) {
            event.setDamage(event.getDamage() * BERSERK_MULTIPLIER);
        }
    }

    private Player resolveAttacker(Entity damager) {
        if (damager instanceof Player player) {
            return player;
        }
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player player) {
            return player;
        }
        return null;
    }
}
