package dev.fabbrini.armorset.abilities;

import dev.fabbrini.armorset.items.ArmorItems;
import dev.fabbrini.armorset.items.ArmorPiece;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Boots of the Dead Souls: permanent Speed II, boosted to Speed V while standing
 * on soul sand/soul soil. The -30% fall damage ability is event-driven and lives
 * in FallDamageListener.
 */
public final class BootsOfDeadSoulsAbilities implements ArmorAbilityModule {

    private static final long TICK_PERIOD = 10L;
    private static final int BASE_AMPLIFIER = 1;   // Speed II
    private static final int SOUL_SAND_AMPLIFIER = 4; // Speed V

    private final Plugin plugin;
    private final ArmorItems armorItems;
    private final Map<UUID, ScheduledTask> tasks = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> onSoulBlock = new ConcurrentHashMap<>();

    public BootsOfDeadSoulsAbilities(Plugin plugin, ArmorItems armorItems) {
        this.plugin = plugin;
        this.armorItems = armorItems;
    }

    @Override
    public void onEquip(Player player) {
        applySpeed(player, BASE_AMPLIFIER);

        UUID id = player.getUniqueId();
        onSoulBlock.put(id, false);
        ScheduledTask task = player.getScheduler().runAtFixedRate(
                plugin, t -> tick(player), () -> cleanup(id), TICK_PERIOD, TICK_PERIOD);
        if (task != null) {
            tasks.put(id, task);
        }
    }

    @Override
    public void onUnequip(Player player) {
        player.removePotionEffect(PotionEffectType.SPEED);

        UUID id = player.getUniqueId();
        ScheduledTask task = tasks.remove(id);
        if (task != null) {
            task.cancel();
        }
        onSoulBlock.remove(id);
    }

    private void cleanup(UUID id) {
        tasks.remove(id);
        onSoulBlock.remove(id);
    }

    private void tick(Player player) {
        if (!armorItems.isWearing(player, ArmorPiece.BOOTS_OF_DEAD_SOULS)) {
            return;
        }

        UUID id = player.getUniqueId();
        boolean nowOnSoulBlock = isOnSoulBlock(player);
        boolean previouslyOnSoulBlock = onSoulBlock.getOrDefault(id, false);
        if (nowOnSoulBlock == previouslyOnSoulBlock) {
            return;
        }

        onSoulBlock.put(id, nowOnSoulBlock);
        applySpeed(player, nowOnSoulBlock ? SOUL_SAND_AMPLIFIER : BASE_AMPLIFIER);
    }

    private boolean isOnSoulBlock(Player player) {
        Block underfoot = player.getLocation().subtract(0, 1, 0).getBlock();
        Material type = underfoot.getType();
        return type == Material.SOUL_SAND || type == Material.SOUL_SOIL;
    }

    private void applySpeed(Player player, int amplifier) {
        // Modern Paper allows multiple effects of the same type to stack, so the old
        // amplifier must be explicitly removed - otherwise addPotionEffect layers a new
        // instance on top instead of replacing it, and the stronger one lingers forever.
        player.removePotionEffect(PotionEffectType.SPEED);
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, amplifier, true, false, false));
    }
}
