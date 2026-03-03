package com.nexorastudios.nxnamegradient.storage;

import java.util.UUID;

public interface Storage {
    void init();
    String load(UUID uuid);
    void save(UUID uuid, String gradientId);
    void close();
}
