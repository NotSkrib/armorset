package dev.fabbrini.armorset.abilities;

import org.bukkit.entity.Player;

/** Reacts to a single armor piece being equipped/unequipped by a player. */
public interface ArmorAbilityModule {

    void onEquip(Player player);

    void onUnequip(Player player);
}
