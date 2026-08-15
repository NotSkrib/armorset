package dev.fabbrini.armorset.listeners;

import dev.fabbrini.armorset.items.ArmorItems;
import dev.fabbrini.armorset.items.ArmorPiece;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Suit of Lies: sneaking twice in quick succession vanishes the wearer for 5 seconds. */
public final class SneakListener implements Listener {

    private static final long DOUBLE_TAP_WINDOW_MS = 400L;
    private static final long VANISH_DURATION_TICKS = 100L;
    private static final long COOLDOWN_MS = 20_000L;

    private final Plugin plugin;
    private final ArmorItems armorItems;

    private final Map<UUID, Long> lastSneakStart = new ConcurrentHashMap<>();
    private final Map<UUID, Long> cooldownUntil = new ConcurrentHashMap<>();
    private final Set<UUID> vanished = ConcurrentHashMap.newKeySet();
    private final Map<UUID, ScheduledTask> restoreTasks = new ConcurrentHashMap<>();

    public SneakListener(Plugin plugin, ArmorItems armorItems) {
        this.plugin = plugin;
        this.armorItems = armorItems;
    }

    @EventHandler
    public void onToggleSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) {
            return;
        }
        Player player = event.getPlayer();
        if (!armorItems.isWearing(player, ArmorPiece.SUIT_OF_LIES)) {
            return;
        }

        long now = System.currentTimeMillis();
        UUID id = player.getUniqueId();
        long previous = lastSneakStart.getOrDefault(id, 0L);
        lastSneakStart.put(id, now);

        if (now - previous <= DOUBLE_TAP_WINDOW_MS) {
            long readyAt = cooldownUntil.getOrDefault(id, 0L);
            if (now < readyAt) {
                long remainingSeconds = (readyAt - now + 999) / 1000;
                player.sendMessage(Component.text(
                        "Vanish is on cooldown for " + remainingSeconds + "s.", NamedTextColor.RED));
                return;
            }
            cooldownUntil.put(id, now + COOLDOWN_MS);
            triggerVanish(player);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player joined = event.getPlayer();
        for (UUID id : vanished) {
            Player hidden = Bukkit.getPlayer(id);
            if (hidden != null) {
                joined.hidePlayer(plugin, hidden);
            }
        }
    }

    private void triggerVanish(Player player) {
        UUID id = player.getUniqueId();
        vanished.add(id);
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!other.equals(player)) {
                other.hidePlayer(plugin, player);
            }
        }

        ScheduledTask existing = restoreTasks.remove(id);
        if (existing != null) {
            existing.cancel();
        }

        ScheduledTask task = player.getScheduler().runDelayed(
                plugin, t -> restoreVisibility(player), () -> restoreVisibility(player), VANISH_DURATION_TICKS);
        if (task != null) {
            restoreTasks.put(id, task);
        }
    }

    private void restoreVisibility(Player player) {
        UUID id = player.getUniqueId();
        vanished.remove(id);
        restoreTasks.remove(id);
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!other.equals(player)) {
                other.showPlayer(plugin, player);
            }
        }
    }
}
