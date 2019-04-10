package com.pinyougou.pay.service.impl;

import com.pinyougou.pojo.TbPayLog;

import java.util.Map;

public interface PayService {

    public abstract Map<String,Object> createNative(String out_trade_no,String total_fee) throws Exception;

    Map<String, String> queryPayStatus(String out_trade_no) throws Exception;

    public TbPayLog getPayLogInRedis(String userId);

    public void updatePayStatusInLog(String out_trade_no, String transaction_id);
}
