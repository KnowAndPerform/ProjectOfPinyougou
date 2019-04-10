package com.pinyougou.cart.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class UserDetailServiceImpl implements UserDetailsService{
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //这里只负责给用户赋予权限需要查询数据库中的角色\权限表\用户

        //因为password交给CAS处理,所以给一个""就可以了;
        List<GrantedAuthority> authentication = new LinkedList<GrantedAuthority>();
        authentication.add(new SimpleGrantedAuthority("ROLE_USER"));
        //给用户授权结束:
        return new User(username,"",authentication);
    }
}
