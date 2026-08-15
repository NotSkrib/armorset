package dev.fabbrini.armorset.abilities;

import dev.fabbrini.armorset.items.ArmorItems;
import dev.fabbrini.armorset.items.ArmorPiece;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Infernal Crown (helmet): permanent Fire Resistance, strips Fire Resistance from
 * nearby foes, and heals the wearer while standing in lava.
 */
public final class InfernalCrownAbilities implements ArmorAbilityModule {

    private static final int STRIP_RADIUS = 5;
    private static final long TICK_PERIOD = 20L;
    private static final double LAVA_HEAL_AMOUNT = 2.0;

    private final Plugin plugin;
    private final ArmorItems armorItems;
    private final Map<UUID, ScheduledTask> tasks = new ConcurrentHashMap<>();

    public InfernalCrownAbilities(Plugin plugin, ArmorItems armorItems) {
        this.plugin = plugin;
        this.armorItems = armorItems;
    }

    @Override
    public void onEquip(Player player) {
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.FIRE_RESISTANCE, PotionEffect.INFINITE_DURATION, 0, true, false, false));

        UUID id = player.getUniqueId();
        ScheduledTask task = player.getScheduler().runAtFixedRate(
                plugin, t -> tick(player), () -> tasks.remove(id), TICK_PERIOD, TICK_PERIOD);
        if (task != null) {
            tasks.put(id, task);
        }
    }

    @Override
    public void onUnequip(Player player) {
        player.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);

        ScheduledTask task = tasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }

    private void tick(Player player) {
        if (!armorItems.isWearing(player, ArmorPiece.INFERNAL_CROWN)) {
            return;
        }
        stripNearbyFireResistance(player);
        healIfInLava(player);
    }

    private void stripNearbyFireResistance(Player player) {
        for (LivingEntity entity : player.getLocation()
                .getNearbyLivingEntities(STRIP_RADIUS, other -> other != player)) {
            if (entity.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)) {
                entity.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
            }
        }
    }

    private void healIfInLava(Player player) {
        Location location = player.getLocation();
        if (location.getBlock().getType() != Material.LAVA) {
            return;
        }
        double max = player.getAttribute(Attribute.MAX_HEALTH).getValue();
        if (player.getHealth() < max) {
            player.setHealth(Math.min(max, player.getHealth() + LAVA_HEAL_AMOUNT));
        }
    }
}
