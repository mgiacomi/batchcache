/*
 * MIT License
 *
 * Copyright (c) 2024 Matt Giacomini
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.gltech.batchcache;

import java.util.List;
import java.util.Map;

/**
 * This interface defines the minimum caching needs of BatchCache and BatchCacheEvict.
 * In practical terms you will need to create a small class that acts as the bridge
 * between BatchCache and your favorite cache provider.
 *
 * @author Matt Giacomini
 */
public interface CacheClient
{
    /**
     * Add or overwrite a value in cache mapped by the provided key.
     *
     * @param key   String to use as the cache key.
     * @param value Object to be cached.
     */
    void set(String key, Object value);

    /**
     * Add or overwrite values in cache mapped by the provided keys.
     * For performance sake, try to use a cache provider that allows
     * bulk sets.
     *
     * @param objectMap Map of keys and objects to be assigned in cache.
     */
    void set(Map<String, Object> objectMap);

    /**
     * Get single value from cache
     *
     * @param key Key to be looked up in cache.
     * @return the cached object or null if no object is found in cache.
     */
    Object get(String key);

    /**
     * Get multiple values from cache
     *
     * @param keys List of keys to be looked up in cache.
     * @return the cached objects by key for objects found in cache.
     */
    Map<String, Object> get(List<String> keys);

    /**
     * Remove a value from cache
     *
     * @param key key of object to be removed from cache.
     */
    void delete(String key);
}
