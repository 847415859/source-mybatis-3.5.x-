package com.tuling.cache;

/**
 * @Description: 缓存接口
 * 仿照 #{@link org.apache.ibatis.cache.Cache}
 * @Date : 2023/10/26 10:25
 * @Auther : tiankun
 */
public interface Cache {

    /**
     * 获取Cache标识
     * @return
     */
    String getId();

    void putObject(Object key, Object value);

    Object getObject(Object key);

    Object removeObject(Object key);

    void clear();

    int getSize();
}
