package com.hmall.cart;
import com.hmall.api.config.DefaultFeignConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.spring.web.plugins.DefaultConfiguration;


@MapperScan
@MapperScan("com.hmall.cart.mapper")
@SpringBootApplication
@EnableFeignClients (basePackages = "com.hmall.api.client",defaultConfiguration = DefaultFeignConfig.class )
//开启feign客户端
public class CartApplication {
    public static void main(String[] args) {
        SpringApplication.run(CartApplication.class, args);
    }


}