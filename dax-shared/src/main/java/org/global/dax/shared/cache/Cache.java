package org.global.dax.shared.cache;

public interface Cache<K, V> {
    V get(K key);
    String[] getAllKeys();
    void put(K key, V value);
    void remove(K key);
    int size();
    void clear();
} 