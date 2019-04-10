package com.pinyougou.sms.controller;


import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.pinyougou.sms.utils.SmsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/sms")
public class SmsController {

    @Autowired
    private SmsUtil smsUtil;
    /**
     * 访问的路径:  http://localhost:9999/sms/sendSms.do   这里边需要一个封装form的对象:
     */
    @RequestMapping(value = "/sendSms" , method = RequestMethod.POST)
    public Map<String,String> sendSms(String phoneNumbers,String signName,String templateCode,String param){
        //调用发送短息的方法 ;
        try {
            SendSmsResponse response = smsUtil.sendSms(phoneNumbers, signName, templateCode, param);
            //封装返回值给map
            Map<String ,String> resultMap = new HashMap<String ,String>();
            resultMap.put("Code",response.getCode());
            resultMap.put("Message",response.getMessage());
            resultMap.put("RequestId",response.getRequestId());
            resultMap.put("BizId",response.getBizId());

            return resultMap;
        } catch (ClientException e) {
            e.printStackTrace();
            return new HashMap<String, String>();
            //这样会返回以个{},
            // 之所以不用null是因为fastjson解析不了null,
        }
    }
}
