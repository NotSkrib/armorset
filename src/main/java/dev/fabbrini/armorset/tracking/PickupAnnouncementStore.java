package dev.fabbrini.armorset.tracking;

import dev.fabbrini.armorset.items.ArmorPiece;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Level;

/**
 * Remembers which legendary pieces have already had their first-pickup announcement,
 * persisted to disk so a server restart doesn't re-announce a piece that's already in
 * circulation.
 */
public final class PickupAnnouncementStore {

    private final Plugin plugin;
    private final File file;
    private final Set<ArmorPiece> announced = EnumSet.noneOf(ArmorPiece.class);

    public PickupAnnouncementStore(Plugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "announced.yml");
        load();
    }

    private void load() {
        if (!file.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (String id : config.getStringList("announced")) {
            ArmorPiece piece = ArmorPiece.fromId(id);
            if (piece != null) {
                announced.add(piece);
            }
        }
    }

    /** Returns true the first time this is called for a piece, false on every call after. */
    public synchronized boolean markAnnounced(ArmorPiece piece) {
        if (!announced.add(piece)) {
            return false;
        }
        save();
        return true;
    }

    private void save() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("announced", announced.stream().map(Enum::name).toList());
        try {
            plugin.getDataFolder().mkdirs();
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save " + file.getName(), e);
        }
    }
}
