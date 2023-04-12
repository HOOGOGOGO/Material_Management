package com.rblue.config;

import com.rblue.common.BaseContext;
import com.rblue.common.exception.InterceptorException;
import com.rblue.entity.Manager;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

@Component
public class LoginHandlerInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //登录成功后，应该有用户的session
        Object loginUser = request.getSession().getAttribute("manager");
        System.out.println("正在通过登录拦截器");
        boolean flag=true;
        if (loginUser == null) {//没有登录
           // return true;
           throw new InterceptorException("访问错误资源");
        }else {
           String[] list1=new String[]{"/resource/index1.html","/resource/index2.html","/checkIn/page","/storage/page"};
           String[] list2=new String[]{"/resource/index0.html","/resource/index2.html","/checkIn/page","/category/page"};
           String[] list3=new String[]{"/resource/index0.html","/resource/index1.html","/storage/page","/category/page"};
            //材料管理员的路径匹配
            Manager manager = (Manager) request.getSession().getAttribute("manager");
            String uri = request.getRequestURI();
            String job=manager.getJob();
            if(job.equals("0")) {
                for (String u : list1) {
                    if (uri.equals(u)) {
                        System.out.println("材料管理员访问错误资源");
                        response.setCharacterEncoding("UTF-8");//设置服务器的编码
                        response.setContentType("text/html; charset = utf-8");//浏览器服务器的编码格式
                        response.getWriter().write("访问错误资源");
                        return false;
                    }
                }
            }else if(job.equals("1")){
                    for (String u:list2) {
                        if (uri.equals(u)){
                            System.out.println("仓库管理员访问错误资源");
                            response.setCharacterEncoding("UTF-8");//设置服务器的编码
                            response.setContentType("text/html; charset = utf-8");//浏览器服务器的编码格式
                            response.getWriter().write("访问错误资源");
                            return false;
                        }
                    }
            }else if(job.equals("2")) {
                for (String u : list3) {
                    if (uri.equals(u)) {
                        System.out.println("系统审核员访问错误资源");
                        response.setCharacterEncoding("UTF-8");//设置服务器的编码
                        response.setContentType("text/html; charset = utf-8");//浏览器服务器的编码格式
                        response.getWriter().write("访问错误资源");
                        return false;
                    }
                }
            }
            //将id存入线程
            BaseContext.setCurrentId(manager.getId());
            System.out.println("当前线程id"+manager.getId());
            return true;
        }
    }
}
