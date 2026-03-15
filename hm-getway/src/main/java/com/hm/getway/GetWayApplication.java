package com.hm.getway;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class GetWayApplication {
    public void main (String args[]){
        SpringApplication.run(GetWayApplication.class,args);
        log.info("getway启动成功");
    }
}
