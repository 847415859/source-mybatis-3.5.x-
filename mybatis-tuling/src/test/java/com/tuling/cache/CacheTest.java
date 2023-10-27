package com.tuling.cache;

import org.junit.Test;

/**
 * @Description:
 * @Date : 2023/10/26 11:02
 * @Auther : tiankun
 */
public class CacheTest {

    @Test
    public void testLruCache(){
        Cache cache = new PerpetualCache("cache-1");
        LruCache lruCache = new LruCache(cache,3);
        lruCache.putObject("1","1");
        lruCache.putObject("2","2");
        lruCache.putObject("3","3");
        lruCache.putObject("4","4");
        lruCache.putObject("5","5");
        lruCache.iteratorItems();
        System.out.println("获取到key为2的值" + lruCache.getObject("2"));
        lruCache.iteratorItems();
        System.out.println("获取到key为3的值" + lruCache.getObject("3"));
        lruCache.iteratorItems();
        System.out.println("获取到key为5的值" + lruCache.getObject("5"));
        lruCache.iteratorItems();
    }
}
