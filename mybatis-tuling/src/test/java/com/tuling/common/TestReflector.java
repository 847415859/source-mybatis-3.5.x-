package com.tuling.common;

import com.tuling.common.bridge.SubClass;
import com.tuling.entity.User;
import org.apache.ibatis.reflection.Reflector;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * @Description: 测试Mybatis 反射器
 * @Date : 2023/10/27 10:04
 * @Auther : tiankun
 */
public class TestReflector {


    @Test
    public void testBridgeMethod(){
        Method[] methods = SubClass.class.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            boolean bridge = method.isBridge();
            System.out.println(String.format("%s 是否是桥接方法 : %s",method.getName(),bridge));
        }
    }

    @Test
    public void test(){
        Reflector reflector = new Reflector(User.class);

    }
}
