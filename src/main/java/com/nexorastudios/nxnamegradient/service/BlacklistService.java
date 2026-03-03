package com.nexorastudios.nxnamegradient.service;

import com.nexorastudios.nxnamegradient.NxNameGradientPlugin;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class BlacklistService {

    public enum BlockReason { NONE, WORLD, REGION, PLAYER, PERMISSION }

    private final NxNameGradientPlugin plugin;

    public BlacklistService(NxNameGradientPlugin plugin) { this.plugin = plugin; }

    public BlockReason check(Player player) {
        if (player == null) return BlockReason.NONE;

        List<String> worlds = plugin.getConfig().getStringList("blacklist.worlds");
        if (player.getWorld() != null) {
            String w = player.getWorld().getName();
            for (String s : worlds) if (s != null && s.equalsIgnoreCase(w)) return BlockReason.WORLD;
        }

        String uuid = player.getUniqueId().toString();
        for (String s : plugin.getConfig().getStringList("blacklist.players")) {
            if (s != null && s.equalsIgnoreCase(uuid)) return BlockReason.PLAYER;
        }

        for (String p : plugin.getConfig().getStringList("blacklist.blocked-permissions")) {
            if (p != null && !p.isBlank() && player.hasPermission(p)) return BlockReason.PERMISSION;
        }

        List<String> regions = plugin.getConfig().getStringList("blacklist.regions");
        if (!regions.isEmpty() && isInBlockedWorldGuardRegion(player, regions)) return BlockReason.REGION;

        return BlockReason.NONE;
    }

    // Optional WorldGuard check via reflection. If WG not present, returns false.
    private boolean isInBlockedWorldGuardRegion(Player player, List<String> blocked) {
        try {
            if (plugin.getServer().getPluginManager().getPlugin("WorldGuard") == null) return false;

            Class<?> bukkitAdapter = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
            Object weWorld = bukkitAdapter.getMethod("adapt", org.bukkit.World.class).invoke(null, player.getWorld());
            Object weLoc = bukkitAdapter.getMethod("adapt", org.bukkit.Location.class).invoke(null, player.getLocation());

            Class<?> wgClass = Class.forName("com.sk89q.worldguard.WorldGuard");
            Object wg = wgClass.getMethod("getInstance").invoke(null);
            Object platform = wgClass.getMethod("getPlatform").invoke(wg);
            Object container = platform.getClass().getMethod("getRegionContainer").invoke(platform);
            Object query = container.getClass().getMethod("createQuery").invoke(container);

            Class<?> weWorldClass = Class.forName("com.sk89q.worldedit.world.World");
            Object regionManager = container.getClass().getMethod("get", weWorldClass).invoke(container, weWorld);
            if (regionManager == null) return false;

            Class<?> weLocClass = Class.forName("com.sk89q.worldedit.util.Location");
            Object applicable = query.getClass().getMethod("getApplicableRegions", weLocClass).invoke(query, weLoc);

            @SuppressWarnings("unchecked")
            Set<Object> regions = (Set<Object>) applicable.getClass().getMethod("getRegions").invoke(applicable);

            for (Object r : regions) {
                String id = (String) r.getClass().getMethod("getId").invoke(r);
                if (id == null) continue;
                String idL = id.toLowerCase(Locale.ROOT);
                for (String b : blocked) {
                    if (b != null && idL.equals(b.toLowerCase(Locale.ROOT))) return true;
                }
            }
        } catch (Throwable ignored) {}
        return false;
    }
}
