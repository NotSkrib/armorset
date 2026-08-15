package dev.fabbrini.armorset.items;

import dev.fabbrini.armorset.ArmorKeys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

/**
 * Single source of truth for building and identifying the 4 legendary armor pieces.
 * Identity is tracked exclusively via a PersistentDataContainer tag - never via
 * display name or lore - so renaming an item client-side can never grant its abilities.
 */
public final class ArmorItems {

    private final ArmorKeys keys;

    public ArmorItems(ArmorKeys keys) {
        this.keys = keys;
    }

    public ItemStack build(ArmorPiece piece) {
        ItemStack item = new ItemStack(piece.material());
        ItemMeta meta = item.getItemMeta();

        meta.displayName(displayName(piece));
        meta.lore(lore(piece));
        meta.setEnchantmentGlintOverride(true);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.getPersistentDataContainer().set(piece.key(keys), PersistentDataType.BOOLEAN, true);

        item.setItemMeta(meta);
        return item;
    }

    public boolean isWearing(Player player, ArmorPiece piece) {
        ItemStack equipped = equippedItem(player, piece.slot());
        if (equipped == null || !equipped.hasItemMeta()) {
            return false;
        }
        Boolean tag = equipped.getItemMeta().getPersistentDataContainer()
                .get(piece.key(keys), PersistentDataType.BOOLEAN);
        return Boolean.TRUE.equals(tag);
    }

    private ItemStack equippedItem(Player player, EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> player.getInventory().getHelmet();
            case CHEST -> player.getInventory().getChestplate();
            case LEGS -> player.getInventory().getLeggings();
            case FEET -> player.getInventory().getBoots();
            default -> null;
        };
    }

    private Component displayName(ArmorPiece piece) {
        Component name = switch (piece) {
            case INFERNAL_CROWN -> Component.text("Infernal Crown", NamedTextColor.GOLD);
            case GHOST_OF_SPARTA -> Component.text("Ghost of Sparta", NamedTextColor.RED);
            case SUIT_OF_LIES -> Component.text("The Suit of Lies", NamedTextColor.DARK_PURPLE);
            case BOOTS_OF_DEAD_SOULS -> Component.text("Boots of the Dead Souls", NamedTextColor.AQUA);
        };
        return name.decoration(TextDecoration.ITALIC, false);
    }

    private List<Component> lore(ArmorPiece piece) {
        String[] lines = switch (piece) {
            case INFERNAL_CROWN -> new String[] {
                    "Permanent Fire Resistance",
                    "Extinguishes Fire Resistance on foes within 5 blocks",
                    "Swimming in lava heals you"
            };
            case GHOST_OF_SPARTA -> new String[] {
                    "Permanent Strength II",
                    "Incoming damage reduced by 20%",
                    "Below 3 hearts: deal 40% more damage"
            };
            case SUIT_OF_LIES -> new String[] {
                    "Greatly increased sneak speed",
                    "High falls transfer damage to nearby players",
                    "Sneak twice quickly to vanish for 5s"
            };
            case BOOTS_OF_DEAD_SOULS -> new String[] {
                    "Permanent Speed II",
                    "Speed V while standing on soul sand or soul soil",
                    "Fall damage reduced by 30%"
            };
        };
        return List.of(lines).stream()
                .<Component>map(line -> Component.text(line, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false))
                .toList();
    }
}
