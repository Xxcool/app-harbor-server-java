package com.example.apppublish;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** 应用包发布服务入口。 */
@MapperScan("com.example.apppublish.mapper")
@SpringBootApplication
public class AppPublishApplication {
  public static void main(String[] args) { SpringApplication.run(AppPublishApplication.class, args); }
}
