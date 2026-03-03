package com.nexorastudios.nxnamegradient.gradient;

import com.nexorastudios.nxnamegradient.NxNameGradientPlugin;
import com.nexorastudios.nxnamegradient.util.ColorUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public final class GradientManager {

    private final NxNameGradientPlugin plugin;
    private final Map<String, Gradient> byId = new LinkedHashMap<>();
    private final Map<Integer, Gradient> byKey = new TreeMap<>();
    private final File gradientsFile;

    public GradientManager(NxNameGradientPlugin plugin) {
        this.plugin = plugin;
        this.gradientsFile = new File(plugin.getDataFolder(), "gradients.yml");
        reload();
    }

    public void reload() {
        byId.clear();
        byKey.clear();

        ConfigurationSection root = plugin.getConfigManager().gradients().getConfigurationSection("gradients");
        if (root == null) return;

        for (String keyStr : root.getKeys(false)) {
            int key;
            try { key = Integer.parseInt(keyStr); }
            catch (NumberFormatException ignored) { continue; }

            ConfigurationSection g = root.getConfigurationSection(keyStr);
            if (g == null) continue;

            String id = g.getString("id", String.valueOf(key));
            if (id == null || id.isBlank()) continue;
            id = id.toLowerCase(Locale.ROOT);

            String name = g.getString("name", id);
            String perm = g.getString("permission", "nxnamegradient.color." + id);

            boolean animated = g.getBoolean("animated", false);
            int speed = g.getInt("speed", 5);

            List<String> colors = g.getStringList("colors");
            if (colors == null || colors.size() < 2) continue;

            List<String> norm = new ArrayList<>();
            for (String c : colors) norm.add(ColorUtil.normalizeHex(c));

            Gradient gradient = new Gradient(key, id, name, perm, animated, speed, norm);
            byKey.put(key, gradient);
            byId.put(id, gradient);
        }
    }

    public List<Gradient> list() { return List.copyOf(byKey.values()); }

    public Gradient getById(String id) {
        if (id == null) return null;
        return byId.get(id.toLowerCase(Locale.ROOT));
    }

    public boolean existsId(String id) { return getById(id) != null; }

    public int nextKeyForNewGradient() {
        int highest = 7;
        for (int k : byKey.keySet()) highest = Math.max(highest, k);
        return highest + 1;
    }

    public Gradient createAndSave(String id, String displayName, String hex1, String hex2) throws Exception {
        id = id.toLowerCase(Locale.ROOT);
        if (existsId(id)) throw new IllegalArgumentException("id_taken");

        int key = nextKeyForNewGradient();

        YamlConfiguration yml = plugin.getConfigManager().gradients();
        String base = "gradients." + key;
        yml.set(base + ".id", id);
        yml.set(base + ".name", displayName);
        yml.set(base + ".permission", "nxnamegradient.color." + id);
        yml.set(base + ".animated", false);
        yml.set(base + ".speed", 5);
        yml.set(base + ".colors", List.of(ColorUtil.normalizeHex(hex1), ColorUtil.normalizeHex(hex2)));
        yml.save(gradientsFile);

        plugin.getConfigManager().reloadAll();
        reload();
        return getById(id);
    }
}
