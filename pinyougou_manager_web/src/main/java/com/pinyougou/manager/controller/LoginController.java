package com.pinyougou.manager.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login")
public class LoginController {

    @RequestMapping("/getLoginName")
    public Map getLoginName(){
        //原来是放在session域中呀.
        //安全容器持有者获取安全容器,在容器中获取认证信息,
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String,String> map = new HashMap<String,String>();
        map.put("loginName",username);
        return map;
    }
}
