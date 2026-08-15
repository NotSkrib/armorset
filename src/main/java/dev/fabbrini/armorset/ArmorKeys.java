package dev.fabbrini.armorset;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

public final class ArmorKeys {

    public final NamespacedKey infernalCrown;
    public final NamespacedKey ghostOfSparta;
    public final NamespacedKey suitOfLies;
    public final NamespacedKey bootsOfDeadSouls;
    public final NamespacedKey sneakSpeedModifier;

    public ArmorKeys(Plugin plugin) {
        this.infernalCrown = new NamespacedKey(plugin, "infernal_crown");
        this.ghostOfSparta = new NamespacedKey(plugin, "ghost_of_sparta");
        this.suitOfLies = new NamespacedKey(plugin, "suit_of_lies");
        this.bootsOfDeadSouls = new NamespacedKey(plugin, "boots_of_dead_souls");
        this.sneakSpeedModifier = new NamespacedKey(plugin, "suit_of_lies_sneak_speed");
    }
}
