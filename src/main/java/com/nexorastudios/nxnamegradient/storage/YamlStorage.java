package com.nexorastudios.nxnamegradient.storage;

import com.nexorastudios.nxnamegradient.NxNameGradientPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.UUID;

public final class YamlStorage implements Storage {

    private final NxNameGradientPlugin plugin;
    private final File file;
    private YamlConfiguration yml;

    public YamlStorage(NxNameGradientPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "playerdata.yml");
        init();
    }

    @Override
    public void init() {
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        yml = YamlConfiguration.loadConfiguration(file);
    }

    @Override
    public String load(UUID uuid) {
        return yml.getString("players." + uuid + ".gradient", null);
    }

    @Override
    public void save(UUID uuid, String gradientId) {
        yml.set("players." + uuid + ".gradient", gradientId);
        try { yml.save(file); } catch (Exception ignored) {}
    }

    @Override
    public void close() {
        try { yml.save(file); } catch (Exception ignored) {}
    }
}
