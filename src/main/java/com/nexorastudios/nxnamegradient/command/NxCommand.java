package com.nexorastudios.nxnamegradient.command;

import com.nexorastudios.nxnamegradient.NxNameGradientPlugin;
import com.nexorastudios.nxnamegradient.gradient.Gradient;
import com.nexorastudios.nxnamegradient.gui.GradientGui;
import com.nexorastudios.nxnamegradient.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Map;

public final class NxCommand implements CommandExecutor {

    private final NxNameGradientPlugin plugin;

    public NxCommand(NxNameGradientPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // /nxnamegradient
        if (args.length == 0) {
            if (!(sender instanceof Player p)) {
                // Console can always see help
                plugin.msg().sendLines(sender, "help");
                return true;
            }
            if (!p.hasPermission("nxnamegradient.gui")) {
                plugin.msg().send(p, "no_permission");
                return true;
            }
            new GradientGui(plugin, p, 0).open();
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        switch (sub) {

            // /nxnamegradient help  (NEW PERMISSION)
            case "help" -> {
                if (sender instanceof Player p) {
                    if (!p.hasPermission("nxnamegradient.help")) {
                        plugin.msg().send(p, "no_permission");
                        return true;
                    }
                }
                plugin.msg().sendLines(sender, "help");
                return true;
            }

            case "gui" -> {
                if (!(sender instanceof Player p)) return true;
                if (!p.hasPermission("nxnamegradient.gui")) { plugin.msg().send(p, "no_permission"); return true; }
                new GradientGui(plugin, p, 0).open();
                return true;
            }

            case "equip" -> {
                if (!(sender instanceof Player p)) return true;
                if (!plugin.getCooldownService().check(p)) { plugin.msg().send(p, "cooldown"); return true; }
                if (args.length < 2) { plugin.msg().send(p, "invalid_id", Map.of("<id>", "?")); return true; }

                Gradient g = plugin.getGradientManager().getById(args[1]);
                if (g == null) { plugin.msg().send(p, "invalid_id", Map.of("<id>", args[1])); return true; }

                var reason = plugin.getBlacklistService().check(p);
                if (reason != com.nexorastudios.nxnamegradient.service.BlacklistService.BlockReason.NONE) {
                    switch (reason) {
                        case WORLD -> plugin.msg().send(p, "blacklisted_world");
                        case REGION -> plugin.msg().send(p, "blacklisted_region");
                        case PLAYER -> plugin.msg().send(p, "blacklisted_player");
                        case PERMISSION -> plugin.msg().send(p, "blacklisted_permission");
                        default -> plugin.msg().send(p, "blacklisted_permission");
                    }
                    plugin.getAnimationManager().stop(p);
                    plugin.getNameService().clear(p);
                    plugin.getDataManager().saveAsync(p.getUniqueId(), null);
                    return true;
                }

                if (!p.hasPermission(g.getPermission())) {
                    plugin.msg().send(p, "no_permission_gradient", Map.of("<gradient>", g.getName(), "<id>", g.getId()));
                    return true;
                }

                plugin.getAnimationManager().apply(p, g);
                plugin.getDataManager().saveAsync(p.getUniqueId(), g.getId());
                plugin.msg().send(p, "equipped", Map.of("<gradient>", g.getName(), "<id>", g.getId()));
                return true;
            }

            case "unequip" -> {
                if (!(sender instanceof Player p)) return true;
                if (!plugin.getCooldownService().check(p)) { plugin.msg().send(p, "cooldown"); return true; }
                plugin.getAnimationManager().stop(p);
                plugin.getNameService().clear(p);
                plugin.getDataManager().saveAsync(p.getUniqueId(), null);
                plugin.msg().send(p, "unequipped");
                return true;
            }

            case "creategradient" -> {
                if (!(sender instanceof Player p)) return true;
                if (!p.hasPermission("nxnamegradient.admin")) { plugin.msg().send(p, "no_permission"); return true; }

                String id, h1, h2;
                if (args.length == 3) {
                    id = "custom" + plugin.getGradientManager().nextKeyForNewGradient();
                    h1 = args[1]; h2 = args[2];
                } else if (args.length >= 4) {
                    id = args[1]; h1 = args[2]; h2 = args[3];
                } else {
                    plugin.msg().send(p, "create_invalid_hex");
                    return true;
                }

                if (!ColorUtil.isHex(h1) || !ColorUtil.isHex(h2)) { plugin.msg().send(p, "create_invalid_hex"); return true; }
                if (plugin.getGradientManager().existsId(id)) { plugin.msg().send(p, "create_id_taken", Map.of("<id>", id)); return true; }

                try {
                    Gradient g = plugin.getGradientManager().createAndSave(id, id, h1, h2);
                    plugin.msg().send(p, "created", Map.of("<gradient>", g.getName(), "<id>", g.getId()));
                } catch (Exception ex) {
                    plugin.msg().send(p, "create_invalid_hex");
                }
                return true;
            }

            case "set" -> {
                if (!sender.hasPermission("nxnamegradient.admin")) { plugin.msg().send(sender, "no_permission"); return true; }
                if (args.length < 3) { plugin.msg().send(sender, "invalid_id", Map.of("<id>", "?")); return true; }

                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) { plugin.msg().send(sender, "player_not_found"); return true; }

                Gradient g = plugin.getGradientManager().getById(args[2]);
                if (g == null) { plugin.msg().send(sender, "invalid_id", Map.of("<id>", args[2])); return true; }

                plugin.getAnimationManager().apply(target, g);
                plugin.getDataManager().saveAsync(target.getUniqueId(), g.getId());
                plugin.msg().send(sender, "admin_set", Map.of("<target>", target.getName(), "<gradient>", g.getName(), "<id>", g.getId()));
                return true;
            }

            case "reset" -> {
                if (!sender.hasPermission("nxnamegradient.admin")) { plugin.msg().send(sender, "no_permission"); return true; }
                if (args.length < 2) { plugin.msg().send(sender, "player_not_found"); return true; }

                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) { plugin.msg().send(sender, "player_not_found"); return true; }

                plugin.getAnimationManager().stop(target);
                plugin.getNameService().clear(target);
                plugin.getDataManager().saveAsync(target.getUniqueId(), null);
                plugin.msg().send(sender, "admin_reset", Map.of("<target>", target.getName()));
                return true;
            }

            case "reload" -> {
                if (!sender.hasPermission("nxnamegradient.reload")) { plugin.msg().send(sender, "no_permission"); return true; }
                plugin.reloadAll();
                plugin.msg().send(sender, "reload_success");
                return true;
            }

            default -> {
                if (sender instanceof Player p) {
                    if (p.hasPermission("nxnamegradient.gui")) {
                        new GradientGui(plugin, p, 0).open();
                        return true;
                    }

                    // Only show help if player has help permission
                    if (p.hasPermission("nxnamegradient.help")) {
                        plugin.msg().sendLines(sender, "help");
                    } else {
                        plugin.msg().send(p, "no_permission");
                    }
                    return true;
                }

                // Console fallback: show help
                plugin.msg().sendLines(sender, "help");
                return true;
            }
        }
    }
}