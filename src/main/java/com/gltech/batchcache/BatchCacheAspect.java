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
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * BatchCacheAspect is a caching concern that is designed with two goals in mind.  First, allow caching to be defined
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
public class BatchCacheAspect
{
    private CacheClient cacheClient;

    /**
     * Aspect method that runs "around" a method annotated with @BatchCache. The method flow is as follows:
     * <ul>
     *     <li>Determine which objects are already in the cache.</li>
     *     <li>Get any missing objects from cache. (some may be in cache some may not)</li>
     *     <li>Cache any objects that were missing from cache</li>
     *     <li>return results</li>
     * </ul>
     *
     * @param joinPoint  JoinPoint provided by the APO Framework.
     * @param batchCache BatchCache annotation provided by the APO Framework.
     * @return the results of the annotated method.
     * @throws Throwable Generic throwable because joinPoint.proceed() could reference anything
     * @see BatchCache
     * @see Around
     */
    @Around(value = "@annotation(batchCache)")
    public Object batchCache(ProceedingJoinPoint joinPoint, BatchCache batchCache) throws Throwable
    {
        if (batchCache.key() == null || batchCache.key().isEmpty())
        {
            throw new IllegalArgumentException("Valid key required for Caching");
        }

        // If we got no arguments then just cache everything with the key name
        if (joinPoint.getArgs().length == 0)
        {
            return getAllForKey(batchCache.key(), joinPoint);
        }
        else if (joinPoint.getArgs()[0] == null)
        {
            return joinPoint.proceed();
        }
        // Key from [] and return Collection
        else if (joinPoint.getArgs().length > 0 && joinPoint.getArgs()[0].getClass().isArray() &&
                (((MethodSignature) joinPoint.getSignature()).getMethod().getReturnType().isAssignableFrom(List.class) ||
                        ((MethodSignature) joinPoint.getSignature()).getMethod().getReturnType().isAssignableFrom(Set.class)))
        {
            return getCollectionFromArray(batchCache, joinPoint);
        }
        // Key from [] and return Map
        else if (joinPoint.getArgs().length > 0 && joinPoint.getArgs()[0].getClass().isArray() &&
                ((MethodSignature) joinPoint.getSignature()).getMethod().getReturnType().isAssignableFrom(Map.class))
        {
            return getMapFromArray(batchCache, joinPoint);
        }
        // Key from List|Set and return Map
        else if (joinPoint.getArgs().length > 0 && joinPoint.getArgs()[0] instanceof Collection &&
                ((MethodSignature) joinPoint.getSignature()).getMethod().getReturnType().isAssignableFrom(Map.class))
        {
            return getMapFromList(batchCache, joinPoint);
        }
        // Key from List|Set and return List|Set
        else if (joinPoint.getArgs().length > 0 && joinPoint.getArgs()[0] instanceof Collection &&
                (((MethodSignature) joinPoint.getSignature()).getMethod().getReturnType().isAssignableFrom(List.class) ||
                        ((MethodSignature) joinPoint.getSignature()).getMethod().getReturnType().isAssignableFrom(Set.class)))
        {
            return getCollectionFromCollection(batchCache, joinPoint);
        }
        // Key from int and return Object
        else if (joinPoint.getArgs().length > 0)
        {
            return getObjectFromObject(batchCache, joinPoint);
        }
        else
        {
            throw new IllegalArgumentException("Missing caching strategy for parameter '" + joinPoint.getArgs()[0].getClass().getName() + "' returning '" + ((MethodSignature) joinPoint.getSignature()).getMethod().getReturnType().getName() + "'");
        }
    }

    private Object getAllForKey(String keyPrefix, ProceedingJoinPoint joinPoint) throws Throwable
    {
        Object cachedValue = cacheClient.get(keyPrefix);

        if (cachedValue != null)
        {
            return cachedValue;
        }
        else
        {
            Object toAdd = joinPoint.proceed();
            cacheClient.set(keyPrefix, toAdd);
            return toAdd;
        }
    }

    private Object getObjectFromObject(BatchCache batchCache, ProceedingJoinPoint joinPoint) throws Throwable
    {
        String key = getKey(batchCache, joinPoint.getArgs()[0]);
        Object cachedValue = cacheClient.get(key);

        if (cachedValue != null)
        {
            return cachedValue;
        }
        else
        {
            Object toAdd = joinPoint.proceed();
            cacheClient.set(key, toAdd);
            return toAdd;
        }
    }

    private Object getCollectionFromCollection(BatchCache batchCache, ProceedingJoinPoint joinPoint) throws IllegalAccessException, InvocationTargetException
    {
        Collection ids = (Collection) joinPoint.getArgs()[0];
        Object keyObjects = ids.stream().map(id -> getKey(batchCache, id)).distinct().collect(Collectors.toList());
        List<String> keys = (List) keyObjects;
        Map<String, Object> cachedValues = cacheClient.get(keys);

        boolean isList = ((MethodSignature) joinPoint.getSignature()).getMethod().getReturnType().isAssignableFrom(List.class);
        Collection results = isList ? new ArrayList() : new HashSet();
        Collection missing = isList ? new ArrayList() : new HashSet();

        for (Object id : ids)
        {
            String key = getKey(batchCache, id);
            if (cachedValues.containsKey(key))
            {
                if (cachedValues.get(key) != null)
                {
                    results.add(cachedValues.get(key));
                }
            }
            else
            {
                missing.add(id);
            }
        }

        if (missing.size() > 0)
        {
            Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
            Object[] methodArgs = joinPoint.getArgs();
            methodArgs[0] = missing;

            Collection toAdd = (Collection) method.invoke(joinPoint.getTarget(), methodArgs);

            for (Object add : toAdd)
            {
                String key = getKey(batchCache, add);
                cacheClient.set(key, add);
            }

            results.addAll(toAdd);
        }

        return results;
    }

    private Collection getCollectionFromArray(BatchCache batchCache, ProceedingJoinPoint joinPoint) throws IllegalAccessException, InvocationTargetException
    {
        ArrayHelper arrayHelper = new ArrayHelper(batchCache, joinPoint);
        List<String> keys = arrayHelper.getKeys();

        Map<String, Object> cachedValues = cacheClient.get(keys);

        boolean isList = ((MethodSignature) joinPoint.getSignature()).getMethod().getReturnType().isAssignableFrom(List.class);
        Collection results = isList ? new ArrayList() : new HashSet();
        Collection missing = isList ? new ArrayList() : new HashSet();

        for (Object id : arrayHelper.getIds())
        {
            String key = getKey(batchCache, id);
            if (cachedValues.containsKey(key))
            {
                if (cachedValues.get(key) != null)
                {
                    results.add(cachedValues.get(key));
                }
            }
            else
            {
                missing.add(id);
            }
        }

        if (missing.size() > 0)
        {
            Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
            Object[] methodArgs = joinPoint.getArgs();
            methodArgs[0] = arrayHelper.getArray(missing);

            Collection toAdd = (Collection) method.invoke(joinPoint.getTarget(), methodArgs);

            for (Object add : toAdd)
            {
                String key = getKey(batchCache, add);
                cacheClient.set(key, add);
            }

            results.addAll(toAdd);
        }

        return results;
    }

    private Map getMapFromArray(BatchCache batchCache, ProceedingJoinPoint joinPoint) throws IllegalAccessException, InvocationTargetException
    {
        ArrayHelper arrayHelper = new ArrayHelper(batchCache, joinPoint);
        List<String> keys = arrayHelper.getKeys();

        Map<String, Object> cachedValues = cacheClient.get(keys);

        Map results = getMapByAssignable(((MethodSignature) joinPoint.getSignature()).getMethod().getReturnType());
        List missing = new ArrayList();

        for (Object id : arrayHelper.getIds())
        {
            String key = getKey(batchCache, id);
            if (cachedValues.containsKey(key))
            {
                if (cachedValues.get(key) != null)
                {
                    results.put(id, cachedValues.get(key));
                }
            }
            else
            {
                missing.add(id);
            }
        }

        if (missing.size() > 0)
        {
            Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
            Object[] methodArgs = joinPoint.getArgs();
            methodArgs[0] = arrayHelper.getArray(missing);

            Map toAdd = (Map) method.invoke(joinPoint.getTarget(), methodArgs);

            for (Object id : missing)
            {
                String key = getKey(batchCache, id);
                cacheClient.set(key, toAdd.get(id));
            }

            results.putAll(toAdd);
        }

        return results;
    }

    private Map getMapFromList(BatchCache batchCache, ProceedingJoinPoint joinPoint) throws IllegalAccessException, InvocationTargetException
    {
        List ids = (List) joinPoint.getArgs()[0];
        Object keyObjects = ids.stream().map(id -> getKey(batchCache, id)).distinct().collect(Collectors.toList());
        List<String> keys = (List) keyObjects;
        Map<String, Object> cachedValues = cacheClient.get(keys);

        Map results = new HashMap<>();
        List missing = new ArrayList<>();

        for (Object id : ids)
        {
            String key = getKey(batchCache, id);
            if (cachedValues.containsKey(key))
            {
                if (cachedValues.get(key) != null)
                {
                    results.put(id, cachedValues.get(key));
                }
            }
            else
            {
                missing.add(id);
            }
        }

        if (missing.size() > 0)
        {
            Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
            Object[] methodArgs = joinPoint.getArgs();
            methodArgs[0] = missing;

            Map toAdd = (Map) method.invoke(joinPoint.getTarget(), methodArgs);

            for (Object id : missing)
            {
                String key = getKey(batchCache, id);
                cacheClient.set(key, toAdd.get(id));
            }

            results.putAll(toAdd);
        }

        return results;
    }

    private static String getKey(BatchCache batchCache, Object object)
    {
        return getKey(batchCache.key(), batchCache.field(), object);
    }

    static String getKey(String prefix, String field, Object object)
    {
        try
        {
            if (object instanceof Integer || object instanceof Long || object instanceof String)
            {
                return prefix + "-" + object;
            }
            if (object instanceof Date)
            {
                return prefix + "-" + ((Date) object).getTime();
            }
            if (field != null && !field.isEmpty())
            {
                String methodName = isRecord(object) ? field : "get" + field.substring(0, 1).toUpperCase() + field.substring(1);
                Method method = object.getClass().getMethod(methodName, (Class<?>[]) null);
                Object result = method.invoke(object, (Object[]) null);

                if (result instanceof Integer || result instanceof Long || result instanceof String)
                {
                    return prefix + "-" + result;
                }
                if (result instanceof Date)
                {
                    return prefix + "-" + ((Date) result).getTime();
                }

                throw new RuntimeException("Method " + methodName + " returned type " + result.getClass().getName() + "  Only int, long, String, and Date are supported.");
            }
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Could not generate Cache key.  Prefix: " + prefix + ",  Field: " + field + ",  Object: " + object.getClass().getName(), e);
        }

        throw new IllegalStateException("Could not generate Cache key.  Prefix: " + prefix + ",  Field: " + field + ",  Object: " + object.getClass().getName());
    }

    private static boolean isRecord(Object base)
    {
        return base != null && base.getClass().isRecord();
    }

    static private class ArrayHelper
    {
        private final BatchCache batchCache;
        private final ProceedingJoinPoint joinPoint;

        public ArrayHelper(BatchCache batchCache, ProceedingJoinPoint joinPoint)
        {
            this.batchCache = batchCache;
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

        public List<String> getKeys()
        {
            return getIds().stream().map(id -> getKey(batchCache, id)).distinct().collect(Collectors.toList());
        }

        public Object getArray(Collection missing)
        {
            if (joinPoint.getArgs()[0].getClass().getComponentType().isAssignableFrom(int.class))
            {
                return missing.stream().mapToInt(id -> (int) id).toArray();
            }
            else if (joinPoint.getArgs()[0].getClass().getComponentType().isAssignableFrom(Integer.class))
            {
                List missingList = new ArrayList(missing);
                Integer[] array = new Integer[missing.size()];
                for (int i = 0; i < missing.size(); i++) array[i] = (Integer) missingList.get(i);
                return array;
            }
            else if (joinPoint.getArgs()[0].getClass().getComponentType().isAssignableFrom(long.class))
            {
                return missing.stream().mapToLong(id -> (long) id).toArray();
            }
            else if (joinPoint.getArgs()[0].getClass().getComponentType().isAssignableFrom(Long.class))
            {
                List missingList = new ArrayList(missing);
                Long[] array = new Long[missing.size()];
                for (int i = 0; i < missing.size(); i++) array[i] = (Long) missingList.get(i);
                return array;
            }
            else if (joinPoint.getArgs()[0].getClass().getComponentType().isAssignableFrom(String.class))
            {
                List missingList = new ArrayList(missing);
                String[] array = new String[missing.size()];
                for (int i = 0; i < missing.size(); i++) array[i] = (String) missingList.get(i);
                return array;
            }

            throw new RuntimeException("Not able to get values from missing: " + joinPoint.getArgs()[0].getClass().getName());
        }
    }

    private Map getMapByAssignable(Class clazz)
    {
        if (clazz.isAssignableFrom(HashMap.class))
        {
            return new HashMap();
        }
        if (clazz.isAssignableFrom(TreeMap.class))
        {
            return new TreeMap();
        }
        if (clazz.isAssignableFrom(Hashtable.class))
        {
            return new Hashtable();
        }

        throw new IllegalArgumentException("Class type " + clazz.getName() + " not supported.");
    }

    /**
     * Set your cache implementation based on CacheClient Interface
     *
     * @param cacheClient Implementation of CacheClient to support get/set/delete.
     * @see CacheClient
     */
    public void setCacheClient(CacheClient cacheClient)
    {
        this.cacheClient = cacheClient;
    }
}
