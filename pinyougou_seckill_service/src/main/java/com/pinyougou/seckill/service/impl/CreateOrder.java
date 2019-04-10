package com.pinyougou.seckill.service.impl;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import utils.IdWorker;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class CreateOrder implements Runnable {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IdWorker idWorker ;
    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private TbSeckillOrderMapper seckillOrderMapper;
    @Override
    public void run() {
        //userId he seckillgoodsId 可以从order中获取:
        Map<String,Object> map = (Map<String, Object>) redisTemplate.boundListOps("seckill_order_queue").rightPop();
        String userId = (String) map.get("userId");
        Long seckillGoodsId = (Long) map.get("seckillGoodsId");
        TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckill_goods").get(seckillGoodsId);
        //线程所需要执行的代码:
        //封装seckill_order数据,存到redis中
        TbSeckillOrder seckillOrder = new TbSeckillOrder();
        seckillOrder.setId(idWorker.nextId());
        seckillOrder.setCreateTime(new Date());
        seckillOrder.setStatus("1");  //未支付
        seckillOrder.setSeckillId(seckillGoodsId);
        seckillOrder.setUserId(userId);
        seckillOrder.setMoney(seckillGoods.getCostPrice());
        seckillOrder.setSellerId(seckillGoods.getSellerId());
        //将预订单存到redis数据库中; 5 分钟;
        redisTemplate.boundValueOps(userId+seckillGoodsId).set(seckillOrder,5L, TimeUnit.MINUTES);
        //下单成功后立减库存,排队人数减一
        seckillGoods.setStockCount(seckillGoods.getStockCount()-1);
        redisTemplate.boundValueOps("seckill_user_queue_"+seckillGoodsId).increment(-1);
        if(seckillGoods.getStockCount()==0 || seckillGoods.getEndTime().getTime()<=new Date().getTime()){
            //将reids中的数据保存到数据库;
            seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
            //商品库存为0,或者时间到了,清除redis中的数据;
            redisTemplate.boundHashOps("seckill_goods").delete(seckillGoodsId);
        }else{
            //库存还有,那么更新reids中的秒杀商品的库存;
            redisTemplate.boundHashOps("seckill_goods").put(seckillGoodsId,seckillGoods);
        }
    }
}
