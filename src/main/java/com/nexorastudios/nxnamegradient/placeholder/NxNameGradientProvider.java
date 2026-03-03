package com.nexorastudios.nxnamegradient.placeholder;

import com.nexorastudios.nxnamegradient.NxNameGradientPlugin;
import com.nexorastudios.nxnamegradient.gradient.Gradient;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public final class NxNameGradientProvider extends PlaceholderExpansion {

    private final NxNameGradientPlugin plugin;

    public NxNameGradientProvider(NxNameGradientPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "nxnamegradient";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Nexora";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";

        String equipped = plugin.getDataManager().getCached(player.getUniqueId());

        if (params.equalsIgnoreCase("has_gradient")) {
            return equipped != null ? "true" : "false";
        }

        if (params.equalsIgnoreCase("current")) {
            return equipped == null ? "" : equipped;
        }

        if (params.equalsIgnoreCase("gradient_name")) {
            if (equipped == null) return "";
            Gradient g = plugin.getGradientManager().getById(equipped);
            return g == null ? "" : g.getName();
        }

        // %nxnamegradient_name%  -> MiniMessage (TAB / Adventure chat)
        if (params.equalsIgnoreCase("name")) {
            if (player.getName() == null) return "";
            if (equipped == null) return player.getName();

            Gradient g = plugin.getGradientManager().getById(equipped);
            if (g == null) return player.getName();

            return "<gradient:" + g.getStartHex() + ":" + g.getEndHex() + ">"
                    + player.getName()
                    + "</gradient>";
        }

        // ✅ %nxnamegradient_name_legacy% -> Works in LPC (legacy hex)
        if (params.equalsIgnoreCase("name_legacy")) {
            if (player.getName() == null) return "";
            if (equipped == null) return player.getName();

            Gradient g = plugin.getGradientManager().getById(equipped);
            if (g == null) return player.getName();

            return toLegacyGradient(player.getName(), g.getStartHex(), g.getEndHex());
        }

        return "";
    }

    private static String toLegacyGradient(String text, String startHex, String endHex) {
        int[] s = hexToRgb(startHex);
        int[] e = hexToRgb(endHex);

        StringBuilder out = new StringBuilder();
        int len = Math.max(1, text.length());

        for (int i = 0; i < text.length(); i++) {
            double t = (len == 1) ? 0.0 : (double) i / (double) (len - 1);

            int r = (int) Math.round(s[0] + (e[0] - s[0]) * t);
            int g = (int) Math.round(s[1] + (e[1] - s[1]) * t);
            int b = (int) Math.round(s[2] + (e[2] - s[2]) * t);

            out.append(toLegacyHex(r, g, b)).append(text.charAt(i));
        }
        return out.toString();
    }

    private static int[] hexToRgb(String hex) {
        String h = hex.startsWith("#") ? hex.substring(1) : hex;
        int r = Integer.parseInt(h.substring(0, 2), 16);
        int g = Integer.parseInt(h.substring(2, 4), 16);
        int b = Integer.parseInt(h.substring(4, 6), 16);
        return new int[]{r, g, b};
    }

    private static String toLegacyHex(int r, int g, int b) {
        String hex = String.format("%02x%02x%02x", r, g, b);
        // §x§R§R§G§G§B§B
        StringBuilder sb = new StringBuilder("§x");
        for (char c : hex.toCharArray()) sb.append('§').append(c);
        return sb.toString();
    }
}