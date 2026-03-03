package com.nexorastudios.nxnamegradient.animation;

import com.nexorastudios.nxnamegradient.NxNameGradientPlugin;
import com.nexorastudios.nxnamegradient.gradient.Gradient;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AnimationManager {

    private final NxNameGradientPlugin plugin;
    private final Map<UUID, AnimationTask> running = new ConcurrentHashMap<>();

    public AnimationManager(NxNameGradientPlugin plugin) { this.plugin = plugin; }

    public void stop(Player player) { if (player != null) stop(player.getUniqueId()); }

    public void stop(UUID uuid) {
        AnimationTask task = running.remove(uuid);
        if (task != null) task.cancel();
    }

    public void stopAll() {
        for (AnimationTask t : running.values()) {
            try { t.cancel(); } catch (Throwable ignored) {}
        }
        running.clear();
    }

    public void apply(Player player, Gradient gradient) {
        if (player == null || gradient == null) return;
        stop(player);
        if (gradient.isAnimated()) {
            AnimationTask task = new AnimationTask(plugin, player.getUniqueId(), gradient);
            running.put(player.getUniqueId(), task);
            task.start();
        } else {
            plugin.getNameService().apply(player, gradient);
        }
    }

    public void applyStored(UUID uuid, String gradientId) {
        if (uuid == null || gradientId == null) return;
        Player p = Bukkit.getPlayer(uuid);
        if (p == null) return;
        Gradient g = plugin.getGradientManager().getById(gradientId);
        if (g == null) return;
        apply(p, g);
    }
}
