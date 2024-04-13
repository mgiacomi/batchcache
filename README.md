# What is the Batch Cache? 

Batch Cache is a annotation based Java library leveraging Spring AOP.  

It's two main design goals are to keep things simple, and support batching.

## What is it's primary use case
It was first written to solve the problem of caching Spring DAO's that were returning bulk data.
This is still it's primary use case, but has also been used for caching calculations on bulk data.

## How is this similar/different than JCache
* Similar in that it uses simple annotations to faciliate caching and eviction
* Similar in that you can use any caching provider you want (Caffiene, Redis, Memcache, etc)
* Different in that it allows you to cache and evict data without writing any code.
* Different in that it is simple and doesn't support evict stragities and such at the annotation level

### How to include BatchCache in your project:

...

### Usage Examples

...
