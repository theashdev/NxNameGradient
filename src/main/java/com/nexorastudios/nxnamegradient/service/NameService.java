package com.nexorastudios.nxnamegradient.service;

import com.nexorastudios.nxnamegradient.NxNameGradientPlugin;
import com.nexorastudios.nxnamegradient.gradient.Gradient;
import com.nexorastudios.nxnamegradient.gradient.GradientEngine;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public final class NameService {

    private final NxNameGradientPlugin plugin;

    public NameService(NxNameGradientPlugin plugin) { this.plugin = plugin; }

    public void apply(Player player, Gradient gradient) {
        if (player == null || gradient == null) return;

        // blacklist enforcement
        var reason = plugin.getBlacklistService().check(player);
        if (reason != BlacklistService.BlockReason.NONE) {
            clear(player);
            return;
        }

        Component comp = GradientEngine.applyGradient(player.getName(), gradient.getColors());

        if (plugin.getConfig().getBoolean("settings.apply-to-displayname", true)) player.displayName(comp);
        if (plugin.getConfig().getBoolean("settings.apply-to-tablist", true)) player.playerListName(comp);
    }

    public void clear(Player player) {
        if (player == null) return;
        Component normal = Component.text(player.getName());
        player.displayName(normal);
        player.playerListName(normal);
    }
}
