package com.nexorastudios.nxnamegradient.animation;

import com.nexorastudios.nxnamegradient.NxNameGradientPlugin;
import com.nexorastudios.nxnamegradient.gradient.Gradient;
import com.nexorastudios.nxnamegradient.gradient.GradientEngine;
import com.nexorastudios.nxnamegradient.util.ColorUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.UUID;

public final class AnimationTask {

    private final NxNameGradientPlugin plugin;
    private final UUID uuid;
    private final Gradient gradient;

    private BukkitTask task;
    private int frame = 0;

    public AnimationTask(NxNameGradientPlugin plugin, UUID uuid, Gradient gradient) {
        this.plugin = plugin;
        this.uuid = uuid;
        this.gradient = gradient;
    }

    public void start() {
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null || !p.isOnline()) { cancel(); return; }

            var reason = plugin.getBlacklistService().check(p);
            if (reason != com.nexorastudios.nxnamegradient.service.BlacklistService.BlockReason.NONE) {
                plugin.getNameService().clear(p);
                plugin.getDataManager().saveAsync(uuid, null);
                cancel();
                return;
            }

            double phase = (frame++ % 100) / 100.0;
            double phase2 = (phase + 0.5) % 1.0;

            String a = lerpHex(gradient.getStartHex(), gradient.getEndHex(), phase);
            String b = lerpHex(gradient.getStartHex(), gradient.getEndHex(), phase2);

            Component comp = GradientEngine.applyGradient(p.getName(), List.of(a, b));
            if (plugin.getConfig().getBoolean("settings.apply-to-displayname", true)) p.displayName(comp);
            if (plugin.getConfig().getBoolean("settings.apply-to-tablist", true)) p.playerListName(comp);
        }, 0L, gradient.getSpeedTicks());
    }

    private String lerpHex(String hexA, String hexB, double t) {
        int[] a = ColorUtil.parseHex(hexA);
        int[] b = ColorUtil.parseHex(hexB);
        int r = (int) Math.round(a[0] + (b[0] - a[0]) * t);
        int g = (int) Math.round(a[1] + (b[1] - a[1]) * t);
        int bb = (int) Math.round(a[2] + (b[2] - a[2]) * t);
        return String.format("#%02x%02x%02x", r, g, bb);
    }

    public void cancel() {
        if (task != null) { task.cancel(); task = null; }
    }
}
