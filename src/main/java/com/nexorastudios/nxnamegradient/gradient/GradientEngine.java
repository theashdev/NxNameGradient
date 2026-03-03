package com.nexorastudios.nxnamegradient.gradient;

import com.nexorastudios.nxnamegradient.util.ColorUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.util.List;

public final class GradientEngine {
    private GradientEngine() {}

    public static Component applyGradient(String text, List<String> colors) {
        if (text == null) text = "";
        if (colors == null || colors.size() < 2) return Component.text(text);

        int[] c1 = ColorUtil.parseHex(colors.get(0));
        int[] c2 = ColorUtil.parseHex(colors.get(colors.size() - 1));

        int len = Math.max(1, text.length());
        Component out = Component.empty();

        for (int i = 0; i < len; i++) {
            double t = (len == 1) ? 0.0 : (double) i / (len - 1);
            int r = (int) Math.round(c1[0] + (c2[0] - c1[0]) * t);
            int g = (int) Math.round(c1[1] + (c2[1] - c1[1]) * t);
            int b = (int) Math.round(c1[2] + (c2[2] - c1[2]) * t);
            out = out.append(Component.text(String.valueOf(text.charAt(i))).color(TextColor.color(r, g, b)));
        }
        return out;
    }
}
