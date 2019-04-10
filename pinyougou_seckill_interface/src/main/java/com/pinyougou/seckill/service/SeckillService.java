package com.pinyougou.seckill.service;

import com.pinyougou.pojo.TbSeckillGoods;

import java.util.List;

public interface SeckillService {

    //秒杀商品展示:
    public List<TbSeckillGoods> displaySeckillGoods();

    TbSeckillGoods findOne(Long seckillGoodsId);

    void createOrder(String userId, Long seckillGoodsId);

}
