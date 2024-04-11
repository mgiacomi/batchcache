package com.gltech.batchcache;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import com.esotericsoftware.kryo.util.Pool;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CacheClientImpl implements CacheClient
{
    private final Cache<String, byte[]> cache;
    private final Pool<Kryo> kryoPool;

    public CacheClientImpl()
    {
        cache = Caffeine.newBuilder().recordStats().expireAfterAccess(1, TimeUnit.DAYS).build();

        // Pool constructor arguments: thread safe, soft references, maximum capacity
        kryoPool = new Pool<>(true, false, 100)
        {
            protected Kryo create()
            {
                Kryo kryo = new Kryo();
                kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
                kryo.setRegistrationRequired(false);
                kryo.setReferences(true);
                return kryo;
            }
        };
    }

    public CacheStats getStats()
    {
        return cache.stats();
    }

    public void set(String key, Object value)
    {
        Kryo kryo = kryoPool.obtain();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); Output output = new Output(baos))
        {
            kryo.writeClassAndObject(output, value);
            output.flush();
            cache.put(key, baos.toByteArray());
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed Serialization", e);
        }
        finally
        {
            kryoPool.free(kryo);
        }
    }

    public void set(Map<String, Object> objectMap)
    {
        Kryo kryo = kryoPool.obtain();
        try
        {
            Map<String, byte[]> bytesMap = objectMap.keySet().stream()
                    .collect(Collectors.toMap(Function.identity(), key ->
                    {
                        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); Output output = new Output(baos))
                        {
                            kryo.writeClassAndObject(output, objectMap.get(key));
                            output.flush();
                            return baos.toByteArray();
                        }
                        catch (IOException e)
                        {
                            throw new RuntimeException("Failed Serialization", e);
                        }
                    }));

            cache.putAll(bytesMap);
        }
        finally
        {
            kryoPool.free(kryo);
        }
    }

    public Object get(String key)
    {
        byte[] bytes = cache.getIfPresent(key);
        if (bytes == null || bytes.length == 0)
        {
            return null;
        }

        Kryo kryo = kryoPool.obtain();
        try (Input input = new Input(bytes))
        {
            return kryo.readClassAndObject(input);
        }
        finally
        {
            kryoPool.free(kryo);
        }
    }

    public Map<String, Object> get(List<String> keys)
    {
        Map<String, byte[]> bytesMap = cache.getAllPresent(keys);

        Kryo kryo = kryoPool.obtain();
        try
        {
            Map<String, Object> objectsMap = new HashMap<>();
            bytesMap.keySet().forEach(key ->
            {
                try (Input input = new Input(bytesMap.get(key)))
                {
                    objectsMap.put(key, kryo.readClassAndObject(input));
                }
            });
            return objectsMap;
        }
        finally
        {
            kryoPool.free(kryo);
        }
    }

    public void delete(String key)
    {
        cache.invalidate(key);
    }

    public void delete(String keyPrefix, Object id)
    {
        cache.invalidate(keyPrefix + "-" + id);
    }

    public void clearAll()
    {
        cache.invalidateAll();
    }
}
