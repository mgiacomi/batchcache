package com.gltech.batchcache;

import java.util.List;
import java.util.Map;

public interface CacheClient
{
    void set(String key, Object value);

    void set(Map<String, Object> objectMap);

    Object get(String key);

    Map<String, Object> get(List<String> keys);

    void delete(String key);
}
