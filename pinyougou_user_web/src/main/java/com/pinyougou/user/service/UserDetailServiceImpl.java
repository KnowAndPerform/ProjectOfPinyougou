package com.pinyougou.user.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserDetailServiceImpl implements UserDetailsService{
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //认证服务类，做认证和授权操作  其中：认证操作，交由cas服务端完成，实现单点登录
        //所以，此认证服务类只做授权操作
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        return new User(username,"",authorities);

    }
}
