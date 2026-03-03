package com.nexorastudios.nxnamegradient.config;

import com.nexorastudios.nxnamegradient.NxNameGradientPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public final class ConfigManager {

    private final NxNameGradientPlugin plugin;

    private File gradientsFile;
    private File guiFile;
    private File messagesFile;

    private YamlConfiguration gradients;
    private YamlConfiguration gui;
    private YamlConfiguration messages;

    public ConfigManager(NxNameGradientPlugin plugin) {
        this.plugin = plugin;
        gradientsFile = new File(plugin.getDataFolder(), "gradients.yml");
        guiFile = new File(plugin.getDataFolder(), "gui.yml");
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        reloadAll();
    }

    public void ensureDefaults() {
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        if (!gradientsFile.exists()) plugin.saveResource("gradients.yml", false);
        if (!guiFile.exists()) plugin.saveResource("gui.yml", false);
        if (!messagesFile.exists()) plugin.saveResource("messages.yml", false);
    }

    public void reloadAll() {
        plugin.reloadConfig();
        gradients = YamlConfiguration.loadConfiguration(gradientsFile);
        gui = YamlConfiguration.loadConfiguration(guiFile);
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public FileConfiguration config() { return plugin.getConfig(); }
    public YamlConfiguration gradients() { return gradients; }
    public YamlConfiguration gui() { return gui; }
    public YamlConfiguration messages() { return messages; }
}
