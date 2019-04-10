package com.pinyougou.seckill.task;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;


/**
 * 此服务器设置了秒杀定时器,用来向redis中存入秒杀数据的;
 */
@Component   //交由spring管理此类
public class SeckillTask {

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    //实际上需要秒杀开始前五分钟执行同步操作,但是由于这是测试,所以只要在秒杀时间内就执行同步操作
//    往往都是6位字符
//    Seconds Minutes Hours DayofMonth Month DayofWeek year
//    秒    分     时     月中某天   月   周中某天   年
    @Scheduled(cron = "0/10 * * * * ?")  //从0秒开始,每隔10秒执行一次
    public void synchronizeSeckillGoodsToRedis(){
        //查询数据库中, 上架商品 & 库存>0的 & 当前时间大于开始时间,并小于秒杀结束时间  即：正在秒杀的商品
        TbSeckillGoodsExample example = new TbSeckillGoodsExample();
        example.createCriteria().andStatusEqualTo("1").andNumGreaterThan(0)
                .andStartTimeLessThanOrEqualTo(new Date())
                .andEndTimeGreaterThanOrEqualTo(new Date());
        List<TbSeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);
        //遍历seckillGoodsList 同步到Redis集合中
        for (TbSeckillGoods tbSeckillGoods : seckillGoodsList) {
            //这里边的getId  就是前段传来的seckillGoodsId
            redisTemplate.boundHashOps("seckill_goods").put(tbSeckillGoods.getId(),tbSeckillGoods);
            //为了避免秒杀商品超卖问题,需要将商品按照数量压栈;
            for(int i=0;i<tbSeckillGoods.getStockCount();i++){
                //有多少个库存就压入多少队列:[1,1,1,1,1,] 因为redis内存有限,所以尽量少放对象,精简;
                redisTemplate.boundListOps("seckill_goods_queue_"+tbSeckillGoods.getId()).leftPush(tbSeckillGoods.getId());
            }
        }
        System.out.println("synchronizedSeckillGoodsToRedis has finished");
    }
}
