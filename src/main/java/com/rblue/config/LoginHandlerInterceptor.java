package com.rblue.config;

import com.rblue.common.BaseContext;
import com.rblue.common.exception.InterceptorException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginHandlerInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //登录成功后，应该有用户的session
        Object loginUser = request.getSession().getAttribute("manager");
        System.out.println("正在通过拦截器");
        if (loginUser == null) {//没有登录
           // return true;
           throw new InterceptorException("访问错误资源");
        } else {
            Long empId = (Long) request.getSession().getAttribute("manager");
            //将id存入线程
            BaseContext.setCurrentId(empId);
            System.out.println("当前线程id"+empId);
            return true;
        }
    }
}
