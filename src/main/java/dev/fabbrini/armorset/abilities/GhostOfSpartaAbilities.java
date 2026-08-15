package dev.fabbrini.armorset.abilities;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Ghost of Sparta (chestplate): permanent Strength II. The -20% incoming damage
 * and berserk +40% outgoing damage abilities are event-driven and live in CombatListener.
 */
public final class GhostOfSpartaAbilities implements ArmorAbilityModule {

    @Override
    public void onEquip(Player player) {
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.STRENGTH, PotionEffect.INFINITE_DURATION, 1, true, false, false));
    }

    @Override
    public void onUnequip(Player player) {
        player.removePotionEffect(PotionEffectType.STRENGTH);
    }
}
