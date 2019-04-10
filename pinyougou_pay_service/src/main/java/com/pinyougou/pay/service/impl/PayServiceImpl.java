package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbPayLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import utils.HttpClient;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PayServiceImpl implements PayService {
    //公众号id
    @Value("${appid}")
    private String appid;
    //商户号id
    @Value("${partner}")
    private String partner;
    //商户密匙
    @Value("${partnerkey}")
    private String partnerkey;
    //回调地址
    @Value("${notifyurl}")
    private String notifyurl;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TbPayLogMapper payLogMapper;
    @Autowired
    private TbOrderMapper orderMapper;
    /**
     * 向微信接口发送请求,获取请求后的内容:
     * @return
     */
    @Override
    public Map<String, Object> createNative(String out_trade_no,String total_fee) throws Exception {
        //1.封装必须的请求参数:
       Map<String , String> params = new HashMap<String, String>();
       params.put("appid",appid);
       params.put("mch_id",partner);
       params.put("nonce_str",WXPayUtil.generateNonceStr());
       params.put("body","品优购");
       params.put("out_trade_no",out_trade_no);
       params.put("total_fee",total_fee);
       params.put("spbill_create_ip","127.0.0.1");
       params.put("notify_url",notifyurl);
       params.put("trade_type","NATIVE");
       params.put("product_id","1");  //itemId需要传过来吧?
        //2.发送请求:  需要xml标签发送需要将map转化成xml
        String paramsXML = WXPayUtil.generateSignedXml(params,partnerkey);
        System.err.println(paramsXML);
        HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
        httpClient.setHttps(true);
        //因为微信接收xml,所以参数需要xml的
        httpClient.setXmlParam(paramsXML);
        httpClient.post();

        //3.处理响应结果,获取响应的内容;
        String resultXML = httpClient.getContent();
        Map<String, String> resultMap = WXPayUtil.xmlToMap(resultXML);

        //前端页面需要交易单号.总金额,和生成二维码的地址连接:
        String code_url = resultMap.get("code_url");
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("code_url",code_url);
        map.put("out_trade_no",out_trade_no);
        map.put("total_fee",total_fee);

        return map;
    }

    /**
     * 通过订单id查询订单的支付状态;
     * @param out_trade_no
     */
    @Override
    public Map<String, String> queryPayStatus(String out_trade_no) throws Exception {
        //通过httpClient调用第三方接口:
        //1.封装请求的参数;
        Map<String,String> params = new HashMap<String, String>();
        params.put("appid",appid);
        params.put("mch_id",partner);
        params.put("out_trade_no",out_trade_no);
        params.put("nonce_str",WXPayUtil.generateNonceStr());
        //2.发送请求
        HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
        String paramsXML = WXPayUtil.generateSignedXml(params,partnerkey);
        //这里边用mapToXml不可以么?
       // String paramsXML = WXPayUtil.mapToXml(params);
        //将参数绑定到httpClient
        httpClient.setXmlParam(paramsXML);
        httpClient.post();
        //3.获取响应结果:
        String content = httpClient.getContent();
        Map<String, String> resultMap = WXPayUtil.xmlToMap(content);
        return resultMap;
    }
    @Override
    public TbPayLog getPayLogInRedis(String userId){
        return (TbPayLog) redisTemplate.boundHashOps("payLog").get(userId);
    }
    /**
     * 更新pay_log中的支付状态:
     */
    @Override
    public void updatePayStatusInLog(String out_trade_no, String transaction_id){
        //支付成功后更新日志的支付状态:
        TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
        payLog.setTransactionId(transaction_id);
        payLog.setCreateTime(new Date());
        payLog.setTradeState("2");  //已支付
        //支付成功后跟新数据库状态:
        payLogMapper.updateByPrimaryKey(payLog);
        //更新所有订单状态:
        String orderList = payLog.getOrderList();
        String[] orderIdList = orderList.split(",");
        for (String orderId : orderIdList) {
            TbOrder tbOrder = orderMapper.selectByPrimaryKey(Long.parseLong(orderId));
            tbOrder.setStatus("2");  //已支付:
            tbOrder.setPaymentTime(new Date());  //设置支付时间:
            //更新到数据库;
            orderMapper.updateByPrimaryKey(tbOrder);
        }
        //订单和支付日志被更新到数据库中后,需要把缓存中的数据清空:
        redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());
    }
}
