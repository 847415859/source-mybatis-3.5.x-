package com.tuling.cache;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @Description: 装饰者模式，将默认实现的 Cache 添加上 Lru的功能
 * @Date : 2023/10/26 10:46
 * @Auther : tiankun
 */
public class LruCache implements Cache{

    static final Object NULL = null;

    private Cache cache;

    private LruCacheAssist assist;

    private int size;


    public LruCache(Cache cache) {
        this(cache,1024);
    }


    public LruCache(Cache cache,int size) {
        this.cache = cache;
        this.size = size;
        assist = new LruCacheAssist(size,0.75F,true);
    }

    @Override
    public String getId() {
        return cache.getId();
    }

    @Override
    public void putObject(Object key, Object value) {
        cache.putObject(key, value);
        assist.put(key,NULL);
        // 如果key淘汰了,则删除原Cache中的键值对
        Object eldestKey = assist.getEldestKey();
        if(eldestKey != null){
            cache.removeObject(eldestKey);
        }
    }

    @Override
    public Object getObject(Object key) {
        // 访问使得其排名靠前
        assist.get(key);
        return cache.getObject(key);
    }

    @Override
    public Object removeObject(Object key) {
        return cache.removeObject(key);
    }

    @Override
    public void clear() {
        cache.clear();
        assist.clear();
    }

    @Override
    public int getSize() {
        return cache.getSize();
    }


    /**
     * 打印缓存中的数据，为了验证
     */
    public void iteratorItems(){
        Set<Object> objects = assist.keySet();
        System.out.println("LRU中的key" + Arrays.toString(new Set[]{objects}));
    }


    /**
     * LRU缓存key,协助类
     */
    class LruCacheAssist extends LinkedHashMap<Object,Object> {

        private static final long serialVersionUID = 4267176411845948333L;

        /**
         * LRU元素大小阈值
         */
        int cacheSize = 10;

        /**
         * 最老的key,需要被删除的key
         */
        Object eldestKey;

        public LruCacheAssist(int initialCapacity, float loadFactor, boolean accessOrder) {
            super(initialCapacity, loadFactor, accessOrder);
            this.cacheSize = initialCapacity;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<Object, Object> eldest) {
            // 达到缓存的阈值
            boolean tooBig = size() > cacheSize;
            if (tooBig) {
                eldestKey = eldest.getKey();
            }
            return tooBig;
        }

        public Object getEldestKey() {
            return eldestKey;
        }
    }
}
