package com.nexorastudios.nxnamegradient.util;

import java.util.regex.Pattern;

public final class ColorUtil {
    private static final Pattern HEX = Pattern.compile("^#?[0-9a-fA-F]{6}$");
    private ColorUtil() {}

    public static boolean isHex(String s) {
        if (s == null) return false;
        return HEX.matcher(s.trim()).matches();
    }

    public static String normalizeHex(String s) {
        if (s == null) return "#ffffff";
        String t = s.trim();
        if (!t.startsWith("#")) t = "#" + t;
        return t.toLowerCase();
    }

    public static int[] parseHex(String hex) {
        String h = normalizeHex(hex);
        int rgb = Integer.parseInt(h.substring(1), 16);
        return new int[] { (rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF };
    }
}
