package com.nexorastudios.nxnamegradient.service;

import com.nexorastudios.nxnamegradient.NxNameGradientPlugin;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CooldownService {
    private final NxNameGradientPlugin plugin;
    private final Map<UUID, Long> last = new ConcurrentHashMap<>();

    public CooldownService(NxNameGradientPlugin plugin) { this.plugin = plugin; }

    public boolean check(Player p) {
        int seconds = Math.max(0, plugin.getConfig().getInt("cooldown-seconds", 0));
        if (seconds == 0) return true;

        long now = System.currentTimeMillis();
        long cd = seconds * 1000L;

        Long l = last.get(p.getUniqueId());
        if (l != null && (now - l) < cd) return false;
        last.put(p.getUniqueId(), now);
        return true;
    }
}
