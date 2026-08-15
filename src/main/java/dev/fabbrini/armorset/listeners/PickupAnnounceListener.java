package dev.fabbrini.armorset.listeners;

import dev.fabbrini.armorset.items.ArmorItems;
import dev.fabbrini.armorset.items.ArmorPiece;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

import java.time.Duration;

/** Broadcasts a server-wide on-screen title when a player picks a legendary piece up off the ground. */
public final class PickupAnnounceListener implements Listener {

    private static final Title.Times TIMES =
            Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500));

    private final ArmorItems armorItems;

    public PickupAnnounceListener(ArmorItems armorItems) {
        this.armorItems = armorItems;
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        ArmorPiece piece = armorItems.identify(event.getItem().getItemStack());
        if (piece == null) {
            return;
        }

        Title title = Title.title(
                Component.text("Legendary Item Found!", NamedTextColor.GOLD),
                Component.text(player.getName() + " picked up " + pieceName(piece), NamedTextColor.YELLOW),
                TIMES);
        Bukkit.getServer().showTitle(title);
    }

    private String pieceName(ArmorPiece piece) {
        return switch (piece) {
            case INFERNAL_CROWN -> "the Infernal Crown";
            case GHOST_OF_SPARTA -> "Ghost of Sparta";
            case SUIT_OF_LIES -> "the Suit of Lies";
            case BOOTS_OF_DEAD_SOULS -> "the Boots of the Dead Souls";
        };
    }
}
