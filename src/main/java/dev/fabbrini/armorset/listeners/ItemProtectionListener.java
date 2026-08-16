package dev.fabbrini.armorset.listeners;

import dev.fabbrini.armorset.items.ArmorItems;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Legendary pieces are irreplaceable (the only source is /armorset give), so a dropped
 * one must never be destroyed by environmental hazards - TNT/creeper explosions, cacti,
 * fire, lava, lightning, etc. Falling into the void still removes it, same as any item.
 */
public final class ItemProtectionListener implements Listener {

    private final ArmorItems armorItems;

    public ItemProtectionListener(ArmorItems armorItems) {
        this.armorItems = armorItems;
    }

    @EventHandler
    public void onItemDamage(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
            return;
        }
        if (!(event.getEntity() instanceof Item item)) {
            return;
        }
        if (armorItems.identify(item.getItemStack()) != null) {
            event.setCancelled(true);
        }
    }
}
