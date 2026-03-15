package com.hmall.item;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.hmall.item.mapper")
@SpringBootApplication
public class itemtApplication {
    public static void main(String[] args) {
        SpringApplication.run(itemtApplication.class, args);
    }
}