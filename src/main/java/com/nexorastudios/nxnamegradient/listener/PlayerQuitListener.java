package com.nexorastudios.nxnamegradient.listener;

import com.nexorastudios.nxnamegradient.NxNameGradientPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerQuitListener implements Listener {

    private final NxNameGradientPlugin plugin;

    public PlayerQuitListener(NxNameGradientPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        // Do NOT modify quit message
        plugin.getAnimationManager().stop(e.getPlayer());
    }
}
