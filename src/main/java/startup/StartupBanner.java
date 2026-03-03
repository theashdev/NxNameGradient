package com.nexorastudios.nxnamegradient.startup;

import com.nexorastudios.nxnamegradient.NxNameGradientPlugin;
import org.bukkit.Bukkit;

public final class StartupBanner {

    private final NxNameGradientPlugin plugin;

    public StartupBanner(NxNameGradientPlugin plugin) {
        this.plugin = plugin;
    }

    public void print() {

        Runtime rt = Runtime.getRuntime();

        long maxMb = rt.maxMemory() / 1024 / 1024;
        long totalMb = rt.totalMemory() / 1024 / 1024;
        long freeMb = rt.freeMemory() / 1024 / 1024;
        long usedMb = totalMb - freeMb;

        int cores = rt.availableProcessors();
        String javaVersion = System.getProperty("java.version");
        String os = System.getProperty("os.name") + " " + System.getProperty("os.version");
        String serverVersion = Bukkit.getVersion();

        var log = plugin.getLogger();

        log.info(" ");
        log.info(" █▄░█ ▀▄▀ █▄░█ ▄▀█ █▀▄▀█ █▀▀ █▀▀ █▀█ ▄▀█ █▀▄ █ █▀▀ █▄░█ ▀█▀");
        log.info(" █░▀█ █░█ █░▀█ █▀█ █░▀░█ ██▄ █▄█ █▀▄ █▀█ █▄▀ █ ██▄ █░▀█ ░█░");
        log.info(" ");
        log.info(" NxNameGradient » Developed by Nexora Studios");
        log.info(" Version » " + plugin.getDescription().getVersion());
        log.info(" ");
        log.info(" ─────────────────────────────────────────────");
        log.info(" Server Specs:");
        log.info("  • Server: " + serverVersion);
        log.info("  • Java: " + javaVersion);
        log.info("  • OS: " + os);
        log.info("  • CPU Cores: " + cores);
        log.info("  • RAM: " + usedMb + "MB / " + totalMb + "MB (Max: " + maxMb + "MB)");
        log.info(" ─────────────────────────────────────────────");
        log.info(" Discord » https://discord.gg/FzK9cnaD2H");
        log.info(" ");
    }
}