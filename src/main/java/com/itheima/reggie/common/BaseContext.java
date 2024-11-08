package com.itheima.reggie.common;
/*
基于ThreadLocal封装工具类，用户保存和获取当前登录用户id,即保存多线程中的公共的变量
*/
public class BaseContext {
    private  static ThreadLocal<Long> threadLocal=new ThreadLocal<>();
    //设置公共区的值
    public static   void  setCurrentId(Long id){
        threadLocal.set(id);
    }

    //取出
    public static Long getCurrentId(){
        return threadLocal.get();
    }
}
