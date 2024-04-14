# What is the Batch Cache? 

Batch Cache is a annotation based Java library leveraging Spring AOP.  

It's two main design goals are to keep things simple, and support batching.

## What is it's primary use case
It was first written to solve the problem of caching Spring DAO's that were performing CRUD operations on bulk data.
This is still it's primary use case, but has also been used for caching calculations on bulk data.

## How is this similar/different than JCache
* Similar in that it uses simple annotations to faciliate caching and eviction
* Similar in that you can use any caching provider you want (Caffiene, Redis, Memcache, etc)
* Different in that it allows you to cache and evict data without writing any code.
* Different in that it is simple and doesn't support evict stragities and such at the annotation level

### How to include BatchCache in your project:

Add the following to your pom.xml

<dependency>
    <groupId>com.gltech</groupId>
    <artifactId>batchcache</artifactId>
    <version>0.9.3</version>
</dependency>

Then configure BatchCacheAspect and BatchCacheEvictAspect in Spring

  <bean id="batchCacheAspect" class="com.gltech.batchcache.BatchCacheAspect">
    <constructor-arg ref="cacheClient"/>
  </bean>

  <bean id="batchCacheEvictAspect" class="com.gltech.batchcache.BatchCacheEvictAspect">
      <constructor-arg ref="cacheClient"/>
  </bean>

If don't have a Cache Client and just want to play around with the framework, then Using Caffeine and Kryo can make for a simple fast in memory cache.
See the implementation in this project as a reference.

https://github.com/mgiacomi/batchcache/blob/main/src/test/java/com/gltech/batchcache/CacheClientImpl.java

### Usage Examples

Documentation coming...
