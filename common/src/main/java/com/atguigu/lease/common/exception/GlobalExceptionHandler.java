package com.atguigu.lease.common.exception;

import com.atguigu.lease.common.result.Result;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

//todo 全局异常处理
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LeaseException.class)
    @ResponseBody
    public Result handle(LeaseException e){
        e.printStackTrace();
        return Result.fail(e.getCode(),e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result handle(Exception e){
        e.getMessage();
        e.printStackTrace();
        return Result.fail();
    }

}
