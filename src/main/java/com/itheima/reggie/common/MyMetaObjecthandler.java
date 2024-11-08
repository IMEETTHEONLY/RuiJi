package com.itheima.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
//必须实现MetaObjectHandler这个方法

/*
自定义元数据对象处理器
*/
@Component
@Slf4j
public class MyMetaObjecthandler implements MetaObjectHandler {
    @Override
    //由于该类无法获取seesion对象，所以说
    public void insertFill(MetaObject metaObject) {
        //插入操作，自动填充
        log.info("公共字段自动填充[insert]...");
        log.info(metaObject.toString());
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("createUser", BaseContext.getCurrentId());
        metaObject.setValue("updateUser", BaseContext.getCurrentId());

    }

    @Override
    public void updateFill(MetaObject metaObject) {
        //更新操作自动填充
        log.info("公共字段自动填充[update]...");

        long id=Thread.currentThread().getId();
        log.info("线程id为:{}",id);

        log.info(metaObject.toString());
        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("updateUser",BaseContext.getCurrentId());
    }
}
