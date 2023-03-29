package com.rblue;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.CrossOrigin;

@Slf4j //日志注解
@SpringBootApplication //boot启动注解
@ServletComponentScan //servlet扫描注解
@EnableTransactionManagement //事务注解
@CrossOrigin
public class MaterialManagermentApplication extends SpringBootServletInitializer {

    //SpringBootServletInitializer主要作用是在打成war包时，放置在tomcat服务器上运行，需要改变启动入口
    public static void main(String[] args) {
        SpringApplication.run(MaterialManagermentApplication.class, args);
        log.info("启动成功");

    }
    // 按照下面的方式重写

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(MaterialManagermentApplication.class);
    }

}
