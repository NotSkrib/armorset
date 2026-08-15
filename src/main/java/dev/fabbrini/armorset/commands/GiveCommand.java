package dev.fabbrini.armorset.commands;

import dev.fabbrini.armorset.items.ArmorItems;
import dev.fabbrini.armorset.items.ArmorPiece;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/** /armorset give <player> <piece> - the sole way to obtain a legendary armor piece. */
public final class GiveCommand implements CommandExecutor, TabCompleter {

    private final ArmorItems armorItems;

    public GiveCommand(ArmorItems armorItems) {
        this.armorItems = armorItems;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("armorset.give")) {
            sender.sendMessage(Component.text("You don't have permission to do that.", NamedTextColor.RED));
            return true;
        }

        if (args.length != 3 || !args[0].equalsIgnoreCase("give")) {
            sender.sendMessage(Component.text("Usage: /armorset give <player> <piece>", NamedTextColor.RED));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(Component.text("Player not found: " + args[1], NamedTextColor.RED));
            return true;
        }

        ArmorPiece piece = ArmorPiece.fromId(args[2]);
        if (piece == null) {
            sender.sendMessage(Component.text(
                    "Unknown piece: " + args[2] + ". Expected one of: infernal_crown, ghost_of_sparta, suit_of_lies, boots_of_dead_souls",
                    NamedTextColor.RED));
            return true;
        }

        target.getInventory().addItem(armorItems.build(piece));
        sender.sendMessage(Component.text(
                "Gave " + target.getName() + " a " + pieceLabel(piece) + ".", NamedTextColor.GREEN));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return filterStartsWith(List.of("give"), args[0]);
        }
        if (args.length == 2) {
            List<String> names = Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
            return filterStartsWith(names, args[1]);
        }
        if (args.length == 3) {
            List<String> ids = Arrays.stream(ArmorPiece.values())
                    .map(piece -> piece.name().toLowerCase(Locale.ROOT))
                    .toList();
            return filterStartsWith(ids, args[2]);
        }
        return List.of();
    }

    private List<String> filterStartsWith(List<String> options, String prefix) {
        String lowerPrefix = prefix.toLowerCase(Locale.ROOT);
        return options.stream().filter(option -> option.toLowerCase(Locale.ROOT).startsWith(lowerPrefix))
                .collect(Collectors.toList());
    }

    private String pieceLabel(ArmorPiece piece) {
        return piece.name().toLowerCase(Locale.ROOT).replace('_', ' ');
    }
}
