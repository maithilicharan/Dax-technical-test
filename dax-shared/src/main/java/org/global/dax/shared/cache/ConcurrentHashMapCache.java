package org.global.dax.shared.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConcurrentHashMapCache implements Cache<String, String> {
    private final ConcurrentMap<String, String> map;

    public ConcurrentHashMapCache() {
        this.map = new ConcurrentHashMap<>();
    }

    @Override
    public String get(String key) {
        return map.get(key);
    }

    @Override
    public String[] getAllKeys() {
        return map.keySet().toArray(new String[0]);
    }

    @Override
    public void put(String key, String value) {
        if (key == null || value == null) {
            throw new IllegalArgumentException("Key and value cannot be null");
        }
        if (key.length() != 4) {
            throw new IllegalArgumentException("Key must be exactly 4 bytes");
        }
        if (value.length() > 2096) {
            throw new IllegalArgumentException("Value cannot exceed 2096 bytes");
        }
        map.put(key, value);
    }

    @Override
    public void remove(String key) {
        map.remove(key);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public void clear() {
        map.clear();
    }
} 