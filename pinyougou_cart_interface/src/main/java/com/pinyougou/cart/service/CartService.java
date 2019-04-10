package com.pinyougou.cart.service;

import groupEntity.Cart;

import java.util.List;

public interface CartService {

    //添加item给购物车
    public List<Cart> addItemToCartList(List<Cart> cartList,String itemId,Integer num);
    //查询redis中的数据;
    List<Cart> findCartListByKey(String key);

    void saveCartListToRedisBySessionId(String sessionId, List<Cart> cartList);

    void saveCartListToRedisByUsername(String username, List<Cart> cartList);


    List<Cart> mergeCartList(List<Cart> cartListBySessionId, List<Cart> cartListByUsername);

    void deleteRedisBySessionId(String sessionId);

}
