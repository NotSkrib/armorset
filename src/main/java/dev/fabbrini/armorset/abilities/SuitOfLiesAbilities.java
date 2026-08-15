package dev.fabbrini.armorset.abilities;

import dev.fabbrini.armorset.ArmorKeys;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;

/**
 * The Suit of Lies (leggings): faster sneaking via an attribute modifier tied to
 * equip/unequip. Fall damage transfer and double-sneak vanish are event-driven and
 * live in FallDamageListener / SneakListener.
 */
public final class SuitOfLiesAbilities implements ArmorAbilityModule {

    private static final double SNEAK_SPEED_BONUS = 0.15;

    private final ArmorKeys keys;

    public SuitOfLiesAbilities(ArmorKeys keys) {
        this.keys = keys;
    }

    @Override
    public void onEquip(Player player) {
        AttributeInstance attribute = player.getAttribute(Attribute.SNEAKING_SPEED);
        if (attribute == null || attribute.getModifier(keys.sneakSpeedModifier) != null) {
            return;
        }
        attribute.addModifier(new AttributeModifier(
                keys.sneakSpeedModifier, SNEAK_SPEED_BONUS, AttributeModifier.Operation.ADD_NUMBER));
    }

    @Override
    public void onUnequip(Player player) {
        AttributeInstance attribute = player.getAttribute(Attribute.SNEAKING_SPEED);
        if (attribute != null) {
            attribute.removeModifier(keys.sneakSpeedModifier);
        }
    }
}
