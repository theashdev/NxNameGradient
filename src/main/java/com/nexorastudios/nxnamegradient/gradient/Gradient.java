package com.nexorastudios.nxnamegradient.gradient;

import java.util.List;

public final class Gradient {
    private final int key;
    private final String id;
    private final String name;
    private final String permission;
    private final boolean animated;
    private final int speedTicks;
    private final List<String> colors;

    public Gradient(int key, String id, String name, String permission, boolean animated, int speedTicks, List<String> colors) {
        this.key = key;
        this.id = id;
        this.name = name;
        this.permission = permission;
        this.animated = animated;
        this.speedTicks = Math.max(1, speedTicks);
        this.colors = colors;
    }

    public int getKey() { return key; }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getPermission() { return permission; }
    public boolean isAnimated() { return animated; }
    public int getSpeedTicks() { return speedTicks; }
    public List<String> getColors() { return colors; }

    public String getStartHex() { return colors.get(0); }
    public String getEndHex() { return colors.get(colors.size() - 1); }
}
