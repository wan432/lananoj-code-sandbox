package com.lanan.lananojcodesandbox.Controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController("/")
public class MainController {
    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
