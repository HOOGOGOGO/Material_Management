package com.rblue.common.exception;

import com.rblue.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理
 */
@ControllerAdvice(annotations = {RestController.class, Controller.class, Component.class})
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 异常处理方法
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex){
        log.error(ex.getMessage());

        if(ex.getMessage().contains("Duplicate entry")){
            String[] split = ex.getMessage().split(" ");
            String msg = split[2].substring(1,split[2].length()-1)+ "已存在";
            return R.error(msg);
        }

        return R.error("未知错误");
    }
    /**
     * 拦截器异常处理方法
     * @return
     */
    @ExceptionHandler(InterceptorException.class)
    public R<String> exceptionHandler(InterceptorException ex){
        log.info("错误信息："+ex.getMessage());
        return R.error(ex.getMessage());
    }


}
