package com.hm.getway.controller;

import com.hm.getway.config.AuthProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;




@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private AuthProperties authProperties;

    @GetMapping("/config")
    public AuthProperties getConfig() {
        return authProperties;
    }
}
