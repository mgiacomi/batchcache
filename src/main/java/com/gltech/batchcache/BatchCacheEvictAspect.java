package com.gltech.batchcache;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Aspect
public class BatchCacheEvictAspect
{
    private CacheClient cacheClient;

    @Around(value = "@annotation(batchCacheEvict)")
    public Object batchCache(ProceedingJoinPoint joinPoint, BatchCacheEvict batchCacheEvict) throws Throwable
    {
        if (batchCacheEvict.key() == null || batchCacheEvict.key().isEmpty())
        {
            throw new IllegalArgumentException("Valid key required for Caching");
        }

        if (joinPoint.getArgs().length == 0)
        {
            return evictAllForKey(batchCacheEvict.key(), joinPoint);
        }
        else if (joinPoint.getArgs()[0] == null)
        {
            return joinPoint.proceed();
        }
        else if (joinPoint.getArgs().length > 0 && joinPoint.getArgs()[0].getClass().isArray())
        {
            return evictFromArray(batchCacheEvict, joinPoint);
        }
        else if (joinPoint.getArgs().length > 0 && joinPoint.getArgs()[0] instanceof Collection)
        {
            return evictFromList(batchCacheEvict, joinPoint);
        }
        // Key from int Object that supports and getId method.
        else if (joinPoint.getArgs().length > 0)
        {
            return evictFromObject(batchCacheEvict, joinPoint);
        }
        else
        {
            throw new IllegalArgumentException("Not able to parse keys from " + joinPoint.getArgs()[0].getClass().getName());
        }
    }

    private Object evictAllForKey(String keyPrefix, ProceedingJoinPoint joinPoint) throws Throwable
    {
        try
        {
            return joinPoint.proceed();
        }
        finally
        {
            Arrays.stream(keyPrefix.replaceAll("\\s", "").split(",")).forEach(cacheClient::delete);
        }
    }

    private Object evictFromList(BatchCacheEvict batchCacheEvict, ProceedingJoinPoint joinPoint) throws Throwable
    {
        Collection ids = (Collection) joinPoint.getArgs()[0];

        try
        {
            return joinPoint.proceed();
        }
        finally
        {
            ids.stream().map(id -> getKeySet(batchCacheEvict, id)).forEach(keys -> ((Set<String>) keys).forEach(cacheClient::delete));
        }
    }

    private Object evictFromObject(BatchCacheEvict batchCacheEvict, ProceedingJoinPoint joinPoint) throws Throwable
    {
        Set<String> keys = getKeySet(batchCacheEvict, joinPoint.getArgs()[0]);

        try
        {
            return joinPoint.proceed();
        }
        finally
        {
            keys.forEach(cacheClient::delete);
        }
    }

    private Object evictFromArray(BatchCacheEvict batchCacheEvict, ProceedingJoinPoint joinPoint) throws Throwable
    {
        ArrayHelper arrayHelper = new ArrayHelper(batchCacheEvict, joinPoint);
        Set<String> keys = arrayHelper.getKeys();

        try
        {
            return joinPoint.proceed();
        }
        finally
        {
            keys.forEach(cacheClient::delete);
        }
    }

    private static Set<String> getKeySet(BatchCacheEvict batchCacheEvict, Object object)
    {
        return Arrays.stream(batchCacheEvict.key().replaceAll("\\s", "").split(","))
                .map(keyPrefix -> BatchCacheAspect.getKey(keyPrefix, batchCacheEvict.field(), object))
                .collect(Collectors.toSet());
    }

    static private class ArrayHelper
    {
        private final BatchCacheEvict batchCacheEvict;
        private final ProceedingJoinPoint joinPoint;

        public ArrayHelper(BatchCacheEvict batchCacheEvict, ProceedingJoinPoint joinPoint)
        {
            this.batchCacheEvict = batchCacheEvict;
            this.joinPoint = joinPoint;
        }

        public List<Object> getIds()
        {
            if (joinPoint.getArgs()[0].getClass().getComponentType().isAssignableFrom(int.class))
            {
                int[] ids = (int[]) joinPoint.getArgs()[0];
                return Arrays.stream(ids).boxed().collect(Collectors.toList());
            }
            else if (joinPoint.getArgs()[0].getClass().getComponentType().isAssignableFrom(Integer.class))
            {
                Integer[] ids = (Integer[]) joinPoint.getArgs()[0];
                return Arrays.stream(ids).collect(Collectors.toList());
            }
            else if (joinPoint.getArgs()[0].getClass().getComponentType().isAssignableFrom(long.class))
            {
                long[] ids = (long[]) joinPoint.getArgs()[0];
                return Arrays.stream(ids).boxed().collect(Collectors.toList());
            }
            else if (joinPoint.getArgs()[0].getClass().getComponentType().isAssignableFrom(Long.class))
            {
                Long[] ids = (Long[]) joinPoint.getArgs()[0];
                return Arrays.stream(ids).collect(Collectors.toList());
            }
            else if (joinPoint.getArgs()[0].getClass().getComponentType().isAssignableFrom(String.class))
            {
                String[] ids = (String[]) joinPoint.getArgs()[0];
                return Arrays.stream(ids).collect(Collectors.toList());
            }

            throw new RuntimeException("Not able to get Keys from class type: " + joinPoint.getArgs()[0].getClass().getName());
        }

        public Set<String> getKeys()
        {
            return getIds().stream().map(id -> getKeySet(batchCacheEvict, id)).flatMap(Set::stream).collect(Collectors.toSet());
        }
    }

    public void setCacheClient(CacheClient cacheClient)
    {
        this.cacheClient = cacheClient;
    }
}
