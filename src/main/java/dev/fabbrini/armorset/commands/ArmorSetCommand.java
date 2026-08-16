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
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * /armorset give <player> <piece|all> - the sole way to obtain a legendary armor piece.
 * /armorset holders <piece> - lists which online players currently have a piece.
 */
public final class ArmorSetCommand implements CommandExecutor, TabCompleter {

    private final Plugin plugin;
    private final ArmorItems armorItems;

    public ArmorSetCommand(Plugin plugin, ArmorItems armorItems) {
        this.plugin = plugin;
        this.armorItems = armorItems;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        return switch (args[0].toLowerCase(Locale.ROOT)) {
            case "give" -> handleGive(sender, args);
            case "holders" -> handleHolders(sender, args);
            default -> {
                sendUsage(sender);
                yield true;
            }
        };
    }

    private boolean handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("armorset.give")) {
            sender.sendMessage(Component.text("You don't have permission to do that.", NamedTextColor.RED));
            return true;
        }

        if (args.length != 3) {
            sender.sendMessage(Component.text("Usage: /armorset give <player> <piece>", NamedTextColor.RED));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(Component.text("Player not found: " + args[1], NamedTextColor.RED));
            return true;
        }

        if (args[2].equalsIgnoreCase("all")) {
            List<ItemStack> items = Arrays.stream(ArmorPiece.values()).map(armorItems::build).toList();
            giveItems(target, items);
            sender.sendMessage(Component.text(
                    "Gave " + target.getName() + " the full legendary armor set.", NamedTextColor.GREEN));
            return true;
        }

        ArmorPiece piece = ArmorPiece.fromId(args[2]);
        if (piece == null) {
            sender.sendMessage(Component.text(
                    "Unknown piece: " + args[2] + ". Expected one of: infernal_crown, ghost_of_sparta, suit_of_lies, boots_of_dead_souls, all",
                    NamedTextColor.RED));
            return true;
        }

        giveItems(target, List.of(armorItems.build(piece)));
        sender.sendMessage(Component.text(
                "Gave " + target.getName() + " a " + pieceLabel(piece) + ".", NamedTextColor.GREEN));
        return true;
    }

    private boolean handleHolders(CommandSender sender, String[] args) {
        if (!sender.hasPermission("armorset.holders")) {
            sender.sendMessage(Component.text("You don't have permission to do that.", NamedTextColor.RED));
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(Component.text("Usage: /armorset holders <piece>", NamedTextColor.RED));
            return true;
        }

        ArmorPiece piece = ArmorPiece.fromId(args[1]);
        if (piece == null) {
            sender.sendMessage(Component.text(
                    "Unknown piece: " + args[1] + ". Expected one of: infernal_crown, ghost_of_sparta, suit_of_lies, boots_of_dead_souls",
                    NamedTextColor.RED));
            return true;
        }

        List<Component> holders = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (armorItems.isWearing(player, piece)) {
                holders.add(Component.text(player.getName() + " (wearing)", NamedTextColor.YELLOW));
            } else if (armorItems.carries(player, piece)) {
                holders.add(Component.text(player.getName() + " (in inventory)", NamedTextColor.GRAY));
            }
        }

        if (holders.isEmpty()) {
            sender.sendMessage(Component.text(
                    "No online player currently has the " + pieceLabel(piece) + ".", NamedTextColor.YELLOW));
            return true;
        }

        sender.sendMessage(Component.text("Holders of the " + pieceLabel(piece) + ":", NamedTextColor.GOLD));
        for (Component holder : holders) {
            sender.sendMessage(Component.text("- ").append(holder));
        }
        return true;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(Component.text("Usage: /armorset give <player> <piece>", NamedTextColor.RED));
        sender.sendMessage(Component.text("Usage: /armorset holders <piece>", NamedTextColor.RED));
    }

    /**
     * target may be owned by a different region thread than whoever ran this command
     * (console, or a player far away), so the inventory mutation must run on target's
     * own thread rather than the command-execution thread.
     */
    private void giveItems(Player target, List<ItemStack> items) {
        target.getScheduler().run(plugin, task -> {
            for (ItemStack item : items) {
                target.getInventory().addItem(item);
            }
        }, () -> {});
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return filterStartsWith(List.of("give", "holders"), args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            List<String> names = Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
            return filterStartsWith(names, args[1]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("holders")) {
            return filterStartsWith(pieceIds(), args[1]);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            List<String> ids = new ArrayList<>(pieceIds());
            ids.add("all");
            return filterStartsWith(ids, args[2]);
        }
        return List.of();
    }

    private List<String> pieceIds() {
        return Arrays.stream(ArmorPiece.values())
                .map(piece -> piece.name().toLowerCase(Locale.ROOT))
                .collect(Collectors.toCollection(ArrayList::new));
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
