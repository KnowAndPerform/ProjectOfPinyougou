package com.pinyougou.user.service;

import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;
import jdk.nashorn.internal.ir.annotations.Reference;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UserDetailsServiceImpl implements UserDetailsService {
    //sellerService 已经在zookeeper中了,但是不能用@Reference注入,因为包扫描扫描不到这个serviceImpl
    //所以用set,通过spring框架注入,到sellerService;`
    private SellerService sellerService;
    //将创建对象交给框架,然后set注入:
    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_SELLER"));
        //根据username查询数据库(实际上username对应数据库中的seller_id):获取password值.
        TbSeller tbSeller = sellerService.findOne(username);
        if(tbSeller!=null && tbSeller.getStatus().equals("1")){
            //当查询出的seller不为空,并且审核已经通过的时候才去拿着数据库中的password比较
            return new User(username,tbSeller.getPassword(),authorities);
        }else{
            return null;
        }

    }
}
