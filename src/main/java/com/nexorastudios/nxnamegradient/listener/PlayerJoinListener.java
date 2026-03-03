package com.nexorastudios.nxnamegradient.listener;

import com.nexorastudios.nxnamegradient.NxNameGradientPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class PlayerJoinListener implements Listener {

    private final NxNameGradientPlugin plugin;

    public PlayerJoinListener(NxNameGradientPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        // Do NOT modify join message
        if (!plugin.getConfig().getBoolean("settings.auto-apply-on-join", true)) return;

        plugin.getDataManager().loadAsync(e.getPlayer().getUniqueId(), storedId -> {
            if (storedId == null) return;

            var reason = plugin.getBlacklistService().check(e.getPlayer());
            if (reason != com.nexorastudios.nxnamegradient.service.BlacklistService.BlockReason.NONE) {
                plugin.getAnimationManager().stop(e.getPlayer());
                plugin.getNameService().clear(e.getPlayer());
                plugin.getDataManager().saveAsync(e.getPlayer().getUniqueId(), null);
                return;
            }

            plugin.getAnimationManager().applyStored(e.getPlayer().getUniqueId(), storedId);
        });
    }
}
