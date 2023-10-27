package com.tuling.common.bridge;

/**
 * @Description:
 * @Date : 2023/10/27 10:19
 * @Auther : tiankun
 */
public class SubClass implements SupperClass<String>{
    @Override
    public void method(String e) {
        System.out.println("e");
    }
}



