package cn.itcast.springboot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(("/test"))
public class HelloWorldController {

    @Autowired
    private Environment environment;

    @GetMapping("/info")
    public String info(){
        return "Hello Spring Boot.url= " + environment.getProperty("url");
    }
}
