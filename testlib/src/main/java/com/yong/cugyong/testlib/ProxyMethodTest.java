package com.yong.cugyong.testlib;

/**
 * Created by cugyong on 2019/1/4.
 */

public class ProxyMethodTest {

    public static void test(Object object){
        // 需要反射的类的全称，含包名
        String proxyClassName = object.getClass().getName() + "$$Proxy";
        try {
            Class<?> clazz = Class.forName(proxyClassName);
            ((ProxyMethod)clazz.newInstance()).proxy(object);
        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }catch (IllegalAccessException e){
            e.printStackTrace();
        }catch (InstantiationException e){
            e.printStackTrace();
        }

    }
}
