package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import groupEntity.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TbOrderItemMapper tbOrderItemMapper;
    @Autowired
    private TbItemMapper tbItemMapper;

    /**
     * 向cartList中添加item
     * @param cartList
     * @param itemId
     * @param num
     * @return
     */
    @Override
    public List<Cart> addItemToCartList(List<Cart> cartList, String itemId, Integer num) {
        TbItem item = tbItemMapper.selectByPrimaryKey(Long.valueOf(itemId));
        if(item==null){
            throw new RuntimeException("商品已下架!");
        }
        if(!"1".equals(item.getStatus())){
            throw new RuntimeException("商品异常,不能购买!");
        }
        //1.判断cartList中没有没此商家,如果有,返回该商家的购物车;
        Cart cart = findSellerIdFromCartList(cartList,item.getSellerId());
        if(cart==null){
            //购物车集合中没有该商家,所以第一次将该商家的商品添加到购物车,所以需要创建属于该商家的购物车;
            cart = new Cart();
            cart.setSellerId(item.getSellerId());
            cart.setSellerName(item.getSeller());
            List<TbOrderItem> orderItemList = new LinkedList<TbOrderItem>();
            //封装一个orderItem,保存到购物车;
            orderItemList.add(getOrderItemByItemAndNum(item,itemId,num));
            cart.setOrderItemList(orderItemList);
            //将封装好的购物车,放到购物车集合中:
            cartList.add(cart);
            //返还新的购物车集合,由controller层放到redis中;
            return cartList;
        }else{
            //购物车有了该商家,还需要判断这个商品是否添加过购物车了:
            //遍历orderItemList
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                if(orderItem.getItemId().longValue() == Long.valueOf(itemId)){
                    //已有这个商品,直接加就好了:
                    orderItem.setNum(orderItem.getNum()+num);
                    orderItem.setTotalFee(new BigDecimal(orderItem.getNum()*orderItem.getPrice().doubleValue()));
                    //如果该商品数量<=0该商品需要从购物车中移除:
                    if(orderItem.getNum()<=0){
                        cart.getOrderItemList().remove(orderItem);
                    }
                    //如果商家的购物车中没有商品了,直接移除商家:
                    if(cart.getOrderItemList().size()==0){
                        cartList.remove(cart);
                    }
                    return cartList;
                }
            }
            //没有加入过购物车:封装加入的商品,将商品加入到该购物车
           TbOrderItem orderItem = getOrderItemByItemAndNum(item,itemId,num);
            cart.getOrderItemList().add(orderItem);
            return cartList;
        }


    }


    /**
     * 判断购物车集合中有没有item对应的商家;
     * @param cartList
     * @param sellerId
     * @return
     */
    private Cart findSellerIdFromCartList(List<Cart> cartList, String sellerId) {
        //遍历购物车集合;
        for (Cart cart : cartList) {
            if(cart.getSellerId().equals(sellerId)){
                return cart;
            }
        }
        return null;
    }
    //通过itemID获取orderItem:
    private TbOrderItem getOrderItemByItemAndNum(TbItem item,String itemId, Integer num) {
        //为了防止某些人设置num=负数:
        if(num<1){
            throw new RuntimeException("至少添加该商家的一件商品到购物车!");
        }
        /**
         `item_id` bigint(20) NOT NULL COMMENT '商品id',
         `goods_id` bigint(20) DEFAULT NULL COMMENT 'SPU_ID',
         `title` varchar(200) COLLATE utf8_bin DEFAULT NULL COMMENT '商品标题',
         `price` decimal(20,2) DEFAULT NULL COMMENT '商品单价',
         `num` int(10) DEFAULT NULL COMMENT '商品购买数量',
         `total_fee` decimal(20,2) DEFAULT NULL COMMENT '商品总金额',
         `pic_path` varchar(200) COLLATE utf8_bin DEFAULT NULL COMMENT '商品图片地址',
         `seller_id` varchar(100) COLLATE utf8_bin DEFAULT NULL,
         */
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setItemId(Long.valueOf(itemId));
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setTitle(item.getTitle());
        orderItem.setPrice(item.getPrice());
        orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()*num));
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setSellerId(item.getSellerId());
        return orderItem;
    }


    @Override
    public void saveCartListToRedisBySessionId(String sessionId, List<Cart> cartList) {
        //如果没有登录,那么保存在redis中7天
        redisTemplate.boundValueOps(sessionId).set(cartList,7L, TimeUnit.DAYS);
    }

    @Override
    public void saveCartListToRedisByUsername(String username, List<Cart> cartList) {
        //如果登陆了,通过username永久保存到redis

        redisTemplate.boundValueOps(username).set(cartList);
    }

    /**
     * 合并redis中的数据;
     * @param cartListBySessionId
     * @param cartListByUsername
     * @return
     */
    @Override
    public List<Cart> mergeCartList(List<Cart> cartListBySessionId, List<Cart> cartListByUsername) {
        //遍历一个加到另一个中就可以了,这个会有bug,需要一个个添加:
        for (Cart cart : cartListBySessionId) {
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                Long itemId = orderItem.getItemId();
                Integer num = orderItem.getNum();
                addItemToCartList(cartListByUsername,itemId.toString(),num);
            }
        }
        return cartListByUsername;
    }
    //删除缓存中的数据
    @Override
    public void deleteRedisBySessionId(String sessionId) {
        redisTemplate.delete(sessionId);
    }

    /**
     * 查询redis中的数据;
     * @param key
     * @return
     */
    @Override
    public List<Cart> findCartListByKey(String key) {
        List<Cart> cartList = (List<Cart>) redisTemplate.boundValueOps(key).get();
        if(cartList==null){
            return new LinkedList<Cart>();
        }
        return cartList;
    }
}
