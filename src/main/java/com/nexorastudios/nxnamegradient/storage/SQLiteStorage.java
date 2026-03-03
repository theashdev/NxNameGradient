package com.nexorastudios.nxnamegradient.storage;

import com.nexorastudios.nxnamegradient.NxNameGradientPlugin;

import java.io.File;
import java.sql.*;
import java.util.UUID;

public final class SQLiteStorage implements Storage {

    private final NxNameGradientPlugin plugin;
    private Connection conn;

    public SQLiteStorage(NxNameGradientPlugin plugin) {
        this.plugin = plugin;
        init();
    }

    @Override
    public void init() {
        try {
            String fileName = plugin.getConfig().getString("storage.sqlite.file", "data.db");
            File dbFile = new File(plugin.getDataFolder(), fileName);
            if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();

            conn = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("CREATE TABLE IF NOT EXISTS nxnamegradient (uuid TEXT PRIMARY KEY, gradient TEXT)");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("SQLite init failed: " + e.getMessage());
        }
    }

    @Override
    public String load(UUID uuid) {
        if (conn == null) return null;
        try (PreparedStatement ps = conn.prepareStatement("SELECT gradient FROM nxnamegradient WHERE uuid=?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("gradient");
            }
        } catch (Exception ignored) {}
        return null;
    }

    @Override
    public void save(UUID uuid, String gradientId) {
        if (conn == null) return;
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO nxnamegradient(uuid, gradient) VALUES(?, ?) " +
                        "ON CONFLICT(uuid) DO UPDATE SET gradient=excluded.gradient")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, gradientId);
            ps.executeUpdate();
        } catch (Exception ignored) {}
    }

    @Override
    public void close() {
        try { if (conn != null) conn.close(); } catch (Exception ignored) {}
    }
}
