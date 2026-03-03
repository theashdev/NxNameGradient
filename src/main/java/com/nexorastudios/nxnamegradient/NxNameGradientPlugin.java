package com.nexorastudios.nxnamegradient;

import com.nexorastudios.nxnamegradient.animation.AnimationManager;
import com.nexorastudios.nxnamegradient.command.NxCommand;
import com.nexorastudios.nxnamegradient.command.NxTabCompleter;
import com.nexorastudios.nxnamegradient.config.ConfigManager;
import com.nexorastudios.nxnamegradient.gradient.GradientManager;
import com.nexorastudios.nxnamegradient.listener.InventoryListener;
import com.nexorastudios.nxnamegradient.listener.PlayerJoinListener;
import com.nexorastudios.nxnamegradient.listener.PlayerQuitListener;
import com.nexorastudios.nxnamegradient.placeholder.NxNameGradientProvider;
import com.nexorastudios.nxnamegradient.service.BlacklistService;
import com.nexorastudios.nxnamegradient.service.CooldownService;
import com.nexorastudios.nxnamegradient.service.MessageService;
import com.nexorastudios.nxnamegradient.service.NameService;
import com.nexorastudios.nxnamegradient.startup.StartupBanner;
import com.nexorastudios.nxnamegradient.storage.DataManager;
import com.nexorastudios.nxnamegradient.storage.SQLiteStorage;
import com.nexorastudios.nxnamegradient.storage.Storage;
import com.nexorastudios.nxnamegradient.storage.YamlStorage;
import org.bukkit.plugin.java.JavaPlugin;

public final class NxNameGradientPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private MessageService messages;

    private GradientManager gradientManager;
    private Storage storage;
    private DataManager dataManager;

    private BlacklistService blacklistService;
    private CooldownService cooldownService;

    private NameService nameService;
    private AnimationManager animationManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.configManager = new ConfigManager(this);
        configManager.ensureDefaults();
        configManager.reloadAll();

        this.messages = new MessageService(this);
        this.gradientManager = new GradientManager(this);

        this.storage = createStorage();
        this.dataManager = new DataManager(this, storage);

        this.blacklistService = new BlacklistService(this);
        this.cooldownService = new CooldownService(this);

        this.nameService = new NameService(this);
        this.animationManager = new AnimationManager(this);

        // Commands
        NxCommand executor = new NxCommand(this);
        NxTabCompleter completer = new NxTabCompleter(this);
        if (getCommand("nxnamegradient") != null) {
            getCommand("nxnamegradient").setExecutor(executor);
            getCommand("nxnamegradient").setTabCompleter(completer);
        }

        // Listeners
        var pm = getServer().getPluginManager();
        pm.registerEvents(new InventoryListener(this), this);
        pm.registerEvents(new PlayerJoinListener(this), this);
        pm.registerEvents(new PlayerQuitListener(this), this);

        // PlaceholderAPI (optional)
        if (pm.getPlugin("PlaceholderAPI") != null) {
            try {
                new NxNameGradientProvider(this).register();
                getLogger().info("PlaceholderAPI hook enabled.");
            } catch (Throwable t) {
                getLogger().warning("PlaceholderAPI hook failed: " + t.getMessage());
            }
        }

        // ✅ Startup Banner (Config Toggle)
        if (getConfig().getBoolean("startup-banner", true)) {
            new StartupBanner(this).print();
        }

        getLogger().info("NxNameGradient v1.2.0 enabled.");
    }

    @Override
    public void onDisable() {
        try { if (animationManager != null) animationManager.stopAll(); } catch (Throwable ignored) {}
        try { if (dataManager != null) dataManager.shutdown(); } catch (Throwable ignored) {}
        getLogger().info("NxNameGradient disabled.");
    }

    private Storage createStorage() {
        String type = getConfig().getString("storage.type", "sqlite").toLowerCase();
        if (type.equals("yaml")) return new YamlStorage(this);
        return new SQLiteStorage(this);
    }

    public void reloadAll() {
        reloadConfig();
        if (configManager != null) configManager.reloadAll();
        if (gradientManager != null) gradientManager.reload();
        if (messages != null) messages.reload();
    }

    public ConfigManager getConfigManager() { return configManager; }
    public MessageService msg() { return messages; }
    public GradientManager getGradientManager() { return gradientManager; }
    public DataManager getDataManager() { return dataManager; }
    public BlacklistService getBlacklistService() { return blacklistService; }
    public CooldownService getCooldownService() { return cooldownService; }
    public NameService getNameService() { return nameService; }
    public AnimationManager getAnimationManager() { return animationManager; }
}