package com.nexorastudios.nxnamegradient.service;

import com.nexorastudios.nxnamegradient.NxNameGradientPlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;

public final class MessageService {
    private final NxNameGradientPlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public MessageService(NxNameGradientPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {}

    public void send(CommandSender to, String key) { send(to, key, null); }

    public void send(CommandSender to, String key, Map<String, String> placeholders) {
        String prefix = plugin.getConfigManager().messages().getString("prefix", "");
        String raw = plugin.getConfigManager().messages().getString(key, "");
        if (raw == null) raw = "";
        String msg = prefix + raw;
        if (placeholders != null) {
            for (var e : placeholders.entrySet()) msg = msg.replace(e.getKey(), e.getValue());
        }
        to.sendMessage(mm.deserialize(msg));
    }

    public void sendLines(CommandSender to, String key) {
        String prefix = plugin.getConfigManager().messages().getString("prefix", "");
        List<String> lines = plugin.getConfigManager().messages().getStringList(key);
        for (String line : lines) to.sendMessage(mm.deserialize(prefix + line));
    }
}
