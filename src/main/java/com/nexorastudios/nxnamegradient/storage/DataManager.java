package com.nexorastudios.nxnamegradient.storage;

import com.nexorastudios.nxnamegradient.NxNameGradientPlugin;
import org.bukkit.Bukkit;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class DataManager {

    private final NxNameGradientPlugin plugin;
    private final Storage storage;
    private final ConcurrentHashMap<UUID, String> cache = new ConcurrentHashMap<>();

    public DataManager(NxNameGradientPlugin plugin, Storage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    public String getCached(UUID uuid) {
        return cache.get(uuid);
    }

    public void loadAsync(UUID uuid, Consumer<String> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String g = storage.load(uuid);
            if (g != null && g.isBlank()) g = null;

            if (g == null) cache.remove(uuid);
            else cache.put(uuid, g);

            String finalG = g;
            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(finalG));
        });
    }

    public void saveAsync(UUID uuid, String gradientId) {
        if (gradientId == null) cache.remove(uuid);
        else cache.put(uuid, gradientId);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> storage.save(uuid, gradientId));
    }

    public void shutdown() { storage.close(); }
}
