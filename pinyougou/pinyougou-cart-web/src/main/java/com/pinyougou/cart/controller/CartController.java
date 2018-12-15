package com.pinyougou.cart.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("/cart")
@RestController
public class CartController {

    @GetMapping("/getUsername")
    public Map<String,Object> getUsername(){
        Map<String,Object> map = new HashMap<>();

        //因为配置了可以匿名访问所以如果是匿名访问的时候，返回的用户名为anonymousUser
        //如果未登录则用户名为：anonymousUser
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        map.put("username",username);
        return map;
    }
}
