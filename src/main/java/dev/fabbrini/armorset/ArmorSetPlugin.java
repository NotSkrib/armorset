package dev.fabbrini.armorset;

import dev.fabbrini.armorset.abilities.ArmorAbilityModule;
import dev.fabbrini.armorset.abilities.BootsOfDeadSoulsAbilities;
import dev.fabbrini.armorset.abilities.GhostOfSpartaAbilities;
import dev.fabbrini.armorset.abilities.InfernalCrownAbilities;
import dev.fabbrini.armorset.abilities.SuitOfLiesAbilities;
import dev.fabbrini.armorset.commands.ArmorSetCommand;
import dev.fabbrini.armorset.items.ArmorItems;
import dev.fabbrini.armorset.items.ArmorPiece;
import dev.fabbrini.armorset.listeners.CombatListener;
import dev.fabbrini.armorset.listeners.FallDamageListener;
import dev.fabbrini.armorset.listeners.ItemProtectionListener;
import dev.fabbrini.armorset.listeners.PickupAnnounceListener;
import dev.fabbrini.armorset.listeners.SneakListener;
import dev.fabbrini.armorset.tracking.EquipmentTracker;
import dev.fabbrini.armorset.tracking.PickupAnnouncementStore;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumMap;
import java.util.Map;

public final class ArmorSetPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        ArmorKeys keys = new ArmorKeys(this);
        ArmorItems armorItems = new ArmorItems(keys);

        Map<ArmorPiece, ArmorAbilityModule> modules = new EnumMap<>(ArmorPiece.class);
        modules.put(ArmorPiece.INFERNAL_CROWN, new InfernalCrownAbilities(this, armorItems));
        modules.put(ArmorPiece.GHOST_OF_SPARTA, new GhostOfSpartaAbilities());
        modules.put(ArmorPiece.SUIT_OF_LIES, new SuitOfLiesAbilities(keys));
        modules.put(ArmorPiece.BOOTS_OF_DEAD_SOULS, new BootsOfDeadSoulsAbilities(this, armorItems));

        EquipmentTracker equipmentTracker = new EquipmentTracker(this, armorItems, modules);
        PickupAnnouncementStore announcementStore = new PickupAnnouncementStore(this);

        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(equipmentTracker, this);
        pluginManager.registerEvents(new CombatListener(armorItems), this);
        pluginManager.registerEvents(new FallDamageListener(armorItems), this);
        pluginManager.registerEvents(new SneakListener(this, armorItems), this);
        pluginManager.registerEvents(new PickupAnnounceListener(armorItems, announcementStore), this);
        pluginManager.registerEvents(new ItemProtectionListener(armorItems), this);

        ArmorSetCommand armorSetCommandExecutor = new ArmorSetCommand(this, armorItems);
        PluginCommand armorSetCommand = getCommand("armorset");
        armorSetCommand.setExecutor(armorSetCommandExecutor);
        armorSetCommand.setTabCompleter(armorSetCommandExecutor);

        for (Player player : getServer().getOnlinePlayers()) {
            equipmentTracker.startTracking(player);
        }
    }

    @Override
    public void onDisable() {
        // Folia automatically cancels every per-entity task scheduled by this plugin.
        // Potion effects/attribute modifiers applied while gear is worn are harmless to
        // leave in place across a reload: EquipmentTracker's re-scan on the next
        // onEnable is idempotent and guards against duplicate attribute modifiers.
    }
}
