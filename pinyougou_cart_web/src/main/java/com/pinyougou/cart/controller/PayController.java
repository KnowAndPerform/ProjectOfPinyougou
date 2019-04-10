package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.impl.PayService;
import com.pinyougou.pojo.TbPayLog;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private PayService payService;
    /**
     * 请求微信支付接口,通过返回的url生成二维码:
     * 前端需要获取返回的url,所以用Map
     * @return
     */
    @RequestMapping("/creatNative")
    public Map<String,Object> creatNative(){
        try{
            //从Redis中获取日志信息;
            String userId = SecurityContextHolder.getContext().getAuthentication().getName();
            TbPayLog payLog = payService.getPayLogInRedis(userId);
            return payService.createNative(payLog.getOutTradeNo(),payLog.getTotalFee()+"");
        }catch (Exception e){
            e.printStackTrace();
            return new HashMap<String, Object>();
        }
    }

    /**
     * 通过订单号查询订单支付状态
     * @return
     */
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        try{
            //设置支付时间是5分钟,如果分钟还没有支付,就显示支付超时!
            int i=0;
            while(i<=100){
                Thread.sleep(3000L);

                Map<String,String> resultMap =  payService.queryPayStatus(out_trade_no);
                if("SUCCESS".equals(resultMap.get("trade_state"))){
                    //支付成功后更新交易日志,和数据库订单状态:
                    //获取微信交易流水号:
                    String transaction_id = resultMap.get("transaction_id");
                    //更新数据库:
                    payService.updatePayStatusInLog(out_trade_no,transaction_id);
                    return new Result(true,"支付成功!");
                }
                i++;
            }
            return new Result(false,"timeout");

        }catch(Exception e){
            e.printStackTrace();
            return new Result(false,"支付失败!");
        }

    }
}
