package com.itheima.reggie.common;

import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

//全局异常处理
@ControllerAdvice(annotations = {RestController.class, Controller.class})  //annotations标记了加了哪些注解的类会被捕捉
@ResponseBody  //这里要返回json类型的数据
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)  //SQLIntegrityConstraintViolationException这
    // 个是sql重复异常，解决什么样的异常
    //异常处理方法
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex){
        log.error(ex.getMessage());

        //详细确定是否是重复导致的异常
        if ((ex.getMessage().contains("Duplicate entry"))){
            //获取重复对象名字
            String[] split=ex.getMessage().split(" ");
            String message=split[2]+"已存在";
           return R.error(message);
        }

        return R.error("未知错误");
    }

    /*异常处理器*/
    @ExceptionHandler(CustomException.class)
    //异常处理方法
    public R<String> exceptionHandler(CustomException ex){
        log.error(ex.getMessage());
        return R.error(ex.getMessage());
    }

}
