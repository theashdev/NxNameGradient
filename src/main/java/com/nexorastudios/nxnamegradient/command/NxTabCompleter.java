package com.nexorastudios.nxnamegradient.command;

import com.nexorastudios.nxnamegradient.NxNameGradientPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public final class NxTabCompleter implements TabCompleter {

    private final NxNameGradientPlugin plugin;

    public NxTabCompleter(NxNameGradientPlugin plugin) { this.plugin = plugin; }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();

        if (args.length == 1) {
            out.add("gui"); out.add("help"); out.add("equip"); out.add("unequip");
            out.add("creategradient"); out.add("set"); out.add("reset"); out.add("reload");
            return out;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("equip")) {
            plugin.getGradientManager().list().forEach(g -> out.add(g.getId()));
            return out;
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("reset"))) {
            plugin.getServer().getOnlinePlayers().forEach(p -> out.add(p.getName()));
            return out;
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            plugin.getGradientManager().list().forEach(g -> out.add(g.getId()));
            return out;
        }

        return out;
    }
}
