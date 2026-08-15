package dev.fabbrini.armorset.tracking;

import dev.fabbrini.armorset.abilities.ArmorAbilityModule;
import dev.fabbrini.armorset.items.ArmorItems;
import dev.fabbrini.armorset.items.ArmorPiece;
import io.papermc.paper.event.entity.EntityEquipmentChangedEvent;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Detects when a player equips/unequips one of the 4 legendary armor pieces and
 * dispatches onEquip/onUnequip to the matching ArmorAbilityModule.
 *
 * A per-player Folia EntityScheduler task (~1s) is the authoritative source of truth,
 * self-healing for equip paths that don't fire an event (dispensers, plugin-set
 * inventories). EntityEquipmentChangedEvent is used as a low-latency fast path for the
 * common case of a player directly swapping gear.
 */
public final class EquipmentTracker implements Listener {

    private static final long SCAN_PERIOD = 20L;

    private final Plugin plugin;
    private final ArmorItems armorItems;
    private final Map<ArmorPiece, ArmorAbilityModule> modules;

    private final Map<UUID, EnumSet<ArmorPiece>> worn = new ConcurrentHashMap<>();
    private final Map<UUID, ScheduledTask> scanTasks = new ConcurrentHashMap<>();

    public EquipmentTracker(Plugin plugin, ArmorItems armorItems, Map<ArmorPiece, ArmorAbilityModule> modules) {
        this.plugin = plugin;
        this.armorItems = armorItems;
        this.modules = new EnumMap<>(modules);
    }

    /**
     * Begins tracking a player. Safe to call from any thread (e.g. onEnable, for
     * already-online players after a /reload) - the first scan runs on the player's
     * own region thread via the entity scheduler rather than inline.
     */
    public void startTracking(Player player) {
        UUID id = player.getUniqueId();
        worn.putIfAbsent(id, EnumSet.noneOf(ArmorPiece.class));

        ScheduledTask task = player.getScheduler().runAtFixedRate(
                plugin, t -> scan(player), () -> forgetTasks(id), 1L, SCAN_PERIOD);
        if (task != null) {
            scanTasks.put(id, task);
        }
    }

    public void stopTracking(Player player) {
        UUID id = player.getUniqueId();

        ScheduledTask task = scanTasks.remove(id);
        if (task != null) {
            task.cancel();
        }

        EnumSet<ArmorPiece> pieces = worn.remove(id);
        if (pieces != null) {
            for (ArmorPiece piece : pieces) {
                modules.get(piece).onUnequip(player);
            }
        }
    }

    private void forgetTasks(UUID id) {
        scanTasks.remove(id);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        startTracking(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        stopTracking(event.getPlayer());
    }

    @EventHandler
    public void onEquipmentChanged(EntityEquipmentChangedEvent event) {
        if (event.getEntity() instanceof Player player) {
            scan(player);
        }
    }

    private void scan(Player player) {
        UUID id = player.getUniqueId();
        EnumSet<ArmorPiece> current = worn.computeIfAbsent(id, k -> EnumSet.noneOf(ArmorPiece.class));

        for (ArmorPiece piece : ArmorPiece.values()) {
            boolean wearing = armorItems.isWearing(player, piece);
            boolean wasWearing = current.contains(piece);

            if (wearing && !wasWearing) {
                current.add(piece);
                modules.get(piece).onEquip(player);
            } else if (!wearing && wasWearing) {
                current.remove(piece);
                modules.get(piece).onUnequip(player);
            }
        }
    }
}
