package org.global.dax.shared;

import org.global.dax.shared.cache.ConcurrentHashMapCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;



public class ConcurrentHashMapCacheTest {

    private ConcurrentHashMapCache cache;

    @BeforeEach
    void setUp() {
        cache = new ConcurrentHashMapCache();
    }

    @Test
    void testPutAndGet() {
        cache.put("key1", "value1");
        assertEquals("value1", cache.get("key1"));
    }

    @Test
    void testGetNonExistentKey() {
        assertNull(cache.get("nonExistentKey"));
    }

    @Test
    void testGetAllKeysEmptyCache() {
        String[] keys = cache.getAllKeys();
        assertEquals(0, keys.length);
    }

    @Test
    void testGetAllKeys() {
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        String[] keys = cache.getAllKeys();
        assertEquals(2, keys.length);
        assertArrayEquals(new String[]{"key1", "key2"}, keys);
    }

    @Test
    void testRemove() {
        cache.put("key1", "value1");
        cache.remove("key1");
        assertNull(cache.get("key1"));
    }

    @Test
    void testRemoveNonExistentKey() {
        // Removing a non-existent key should not throw an error
        cache.remove("nonExistentKey");
        assertEquals(0, cache.size());
    }

    @Test
    void testSizeEmptyCache() {
        assertEquals(0, cache.size());
    }

    @Test
    void testSize() {
        cache.put("key1", "value1");
        assertEquals(1, cache.size());
    }

    @Test
    void testClear() {
        cache.put("key1", "value1");
        cache.clear();
        assertEquals(0, cache.size());
        assertNull(cache.get("key1"));
    }

    @Test
    void testPutNullKeyThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> cache.put(null, "value1"));
    }

    @Test
    void testPutNullValueThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> cache.put("key1", null));
    }

    @Test
    void testPutInvalidKeyLengthThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> cache.put("key", "value1"));
        assertThrows(IllegalArgumentException.class, () -> cache.put("key12345", "value1"));
    }

    @Test
    void testPutValueTooLongThrowsException() {
        String longValue = "a".repeat(2097);
        assertThrows(IllegalArgumentException.class, () -> cache.put("key1", longValue));
    }

    @Test
    void testPutValidKeyAndValue() {
        assertDoesNotThrow(() -> cache.put("abcd", "value"));
        assertEquals("value", cache.get("abcd"));
    }

     @Test
     void testGetAllKeysReturnsCopy() {
         cache.put("key1", "value1");
         String[] keys = cache.getAllKeys();
         keys[0] = "modified"; // Modify the returned array
         assertEquals("key1", cache.getAllKeys()[0]);
     }
}
