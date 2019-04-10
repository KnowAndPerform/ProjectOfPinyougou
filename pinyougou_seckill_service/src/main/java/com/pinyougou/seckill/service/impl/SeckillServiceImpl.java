package com.pinyougou.seckill.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.Transactional;
import utils.IdWorker;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class SeckillServiceImpl implements SeckillService {


    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ThreadPoolTaskExecutor executor;
    @Autowired
    private CreateOrder createOrder;

    @Override
    public List<TbSeckillGoods> displaySeckillGoods() {
        //所有的秒杀商品,已经存到了redis中,所以直接在Redis中找就可以了:
        return redisTemplate.boundHashOps("seckill_goods").values();
    }

    @Override
    public TbSeckillGoods findOne(Long seckillGoodsId) {

        return (TbSeckillGoods) redisTemplate.boundHashOps("seckill_goods").get(seckillGoodsId);
    }

    /**
     * 生成一个预订单,保存到redis中,当支付成功后,=再从Redis更新到数据库;
     * @param userId
     * @param seckillGoodsId
     */
    @Override
    public void createOrder(String userId, Long seckillGoodsId) {
        //秒杀商品只允许一个用户抢一个:
        TbSeckillOrder order  = (TbSeckillOrder) redisTemplate.boundValueOps(userId + seckillGoodsId).get();
        if(order!=null){
            //此商品该用户已经有了,所以抛异常;
            throw new RuntimeException("你已经提交/购买了此商品,不能重复秒杀");
        }
        //进来一个人,排队人数加一:
        redisTemplate.boundValueOps("seckill_user_queue_"+seckillGoodsId).increment(1);
        //为了防止超卖问题从redis队列中弹出商品,弹完队列心中就没有了,
        Object rightPop = redisTemplate.boundListOps("seckill_goods_queue_" + seckillGoodsId).rightPop();
        //从redis中取出此秒杀商品:秒杀商品的库存-1;
       /* TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckill_goods").get(seckillGoodsId);*/
        if(rightPop==null){
            //库存不足: 下边都不执行;
            throw  new RuntimeException("秒杀商品已售罄!");
        }
        //获取当前商品的排队人数:
        Long size = redisTemplate.boundValueOps("seckill_user_queue_" + seckillGoodsId).size();
        //既然代码运行到这代表从商品还有,所以从redis中获得:
        TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckill_goods").get(seckillGoodsId);
        if(size>seckillGoods.getStockCount()*2){
            throw new RuntimeException("排队人数过多!");
        }

        //将String userId, Long seckillGoodsId 以map形式存到队列中,多线程从队列中pop数据执行:
        Map<String , Object> map = new HashMap<String, Object>();
        map.put("userId",userId);
        map.put("seckillGoodsId",seckillGoodsId);
        redisTemplate.boundListOps("seckill_order_queue").leftPush(map);

        executor.execute(createOrder);
    }
}
