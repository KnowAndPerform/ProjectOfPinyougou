package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.seckill.service.SeckillService;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/seckill")
public class SeckillController {

    @Reference
    private SeckillService seckillService;
    /**
     * 秒杀商品的展示:
     * @return
     */
    @RequestMapping("/displaySeckillGoods")
    public List<TbSeckillGoods> displaySeckillGoods(){

        return seckillService.displaySeckillGoods();
    }

    /**
     * 查看秒杀商品详情:
     * @param seckillGoodsId
     * @return
     */
    @RequestMapping("/findOne")
    public TbSeckillGoods findOne(Long seckillGoodsId){
        return seckillService.findOne(seckillGoodsId);
    }
    @RequestMapping("/createOrder")
    public Result createOrder(Long seckillGoodsId){
        try{
            //需要根据userId 存订单:
            String userId = SecurityContextHolder.getContext().getAuthentication().getName();
            if("anonymousUser".equals(userId)){
                return new Result(false,"抢单前请先登录!");
            }
            seckillService.createOrder(userId,seckillGoodsId);
            return new Result(true,"下单成功,请在5分钟之内支付!");
        }catch(RuntimeException re){
            re.printStackTrace();
            return new Result(false,re.getMessage());
        }catch(Exception e){
            e.printStackTrace();
            return new Result(false,"订单生成失败!");
        }
    }
}
