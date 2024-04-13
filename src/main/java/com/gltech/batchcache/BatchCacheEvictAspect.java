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

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * BatchCacheEvictAspect is a caching concern that is designed with two goals in mind.  First, allow caching to be defined
 * as simple annotations on methods (JCache or SpringCache Style) and have the leverage the performance of batching.
 *
 * <p>It is designed and tested for Aspect Oriented Annotations in Spring.
 *
 * <p><a href="https://docs.spring.io/spring-framework/reference/core/aop.html">Aspect Oriented Programming with Spring</a>
 *
 * @author Matt Giacomini
 * @see Aspect
 */
@Aspect
public class BatchCacheEvictAspect
{
    private final CacheClient cacheClient;

    /**
     * Set your cache implementation based on CacheClient Interface
     *
     * @param cacheClient Implementation of CacheClient to support get/set/delete.
     * @see CacheClient
     */
    public BatchCacheEvictAspect(CacheClient cacheClient)
    {
        this.cacheClient = cacheClient;
    }


    /**
     * Aspect method that runs "around" a method annotated with @BatchCacheEvict. This method simply needs to find out
     * which objects need to be evicted and evict them from cache.
     *
     * @param joinPoint       JoinPoint provided by the APO Framework.
     * @param batchCacheEvict BatchCacheEvict annotation provided by the APO Framework.
     * @return the results of the annotated method.
     * @throws Throwable Generic throwable because joinPoint.proceed() could reference anything
     * @see BatchCacheEvict
     * @see Around
     */
    @Around(value = "@annotation(batchCacheEvict)")
    public Object batchCacheEvict(ProceedingJoinPoint joinPoint, BatchCacheEvict batchCacheEvict) throws Throwable
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
}
