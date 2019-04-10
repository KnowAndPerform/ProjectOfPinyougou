package com.pinyougou.user.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login")
public class LoginController {

    /**
     * 基于安全框架获取登录人登录名信息
     * {loginName:"admin"}
     */
    @RequestMapping("/getLoginName")
    public Map<String,String> getLoginName(){
        //基于安全框架获取登录人登录名
        String loginName = SecurityContextHolder.getContext().getAuthentication().getName();

        Map<String,String> map = new HashMap<String,String>();
        map.put("loginName",loginName);

        return map;

    }
}
