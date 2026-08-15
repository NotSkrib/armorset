package dev.fabbrini.armorset.items;

import dev.fabbrini.armorset.ArmorKeys;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.EquipmentSlot;

public enum ArmorPiece {
    INFERNAL_CROWN(Material.NETHERITE_HELMET, EquipmentSlot.HEAD),
    GHOST_OF_SPARTA(Material.NETHERITE_CHESTPLATE, EquipmentSlot.CHEST),
    SUIT_OF_LIES(Material.NETHERITE_LEGGINGS, EquipmentSlot.LEGS),
    BOOTS_OF_DEAD_SOULS(Material.NETHERITE_BOOTS, EquipmentSlot.FEET);

    private final Material material;
    private final EquipmentSlot slot;

    ArmorPiece(Material material, EquipmentSlot slot) {
        this.material = material;
        this.slot = slot;
    }

    public Material material() {
        return material;
    }

    public EquipmentSlot slot() {
        return slot;
    }

    public NamespacedKey key(ArmorKeys keys) {
        return switch (this) {
            case INFERNAL_CROWN -> keys.infernalCrown;
            case GHOST_OF_SPARTA -> keys.ghostOfSparta;
            case SUIT_OF_LIES -> keys.suitOfLies;
            case BOOTS_OF_DEAD_SOULS -> keys.bootsOfDeadSouls;
        };
    }

    /** Parses the command-friendly id used by /armorset give (e.g. "infernal_crown"). */
    public static ArmorPiece fromId(String id) {
        for (ArmorPiece piece : values()) {
            if (piece.name().equalsIgnoreCase(id)) {
                return piece;
            }
        }
        return null;
    }
}
