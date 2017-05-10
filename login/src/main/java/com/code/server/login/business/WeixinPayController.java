package com.code.server.login.business;

import com.code.server.login.business.util.Configure;
import com.code.server.login.business.util.RandomStringGenerator;
import com.code.server.login.business.util.Signature;

import org.junit.Test;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by Administrator on 2017/4/12.
 */
@Controller("weixinPayController")
@RequestMapping("/api/weixin")
public class WeixinPayController {

    @Test
    public void UnderOrder(){

        Map<String,Object> packageParams = new HashMap<String, Object>();
        packageParams.put("appid", Configure.getAppid());//appID       应用id
        packageParams.put("mch_id",Configure.getMchid());//appID   商户号
        packageParams.put("nonce_str", RandomStringGenerator.getRandomStringByLength(32));//32位随机数
        packageParams.put("body","测试");//商品描述
        String out_trade_no = RandomStringGenerator.getRandomStringByLength(32);
        packageParams.put("out_trade_no",out_trade_no);
        packageParams.put("total_fee","100");//充值金额
        packageParams.put("spbill_create_ip","192.168.1.150");//终端IP
        packageParams.put("trade_type","APP");//支付类型
        packageParams.put("notify_url","123456789");//通知地址

        String signature  =  Signature.getSign(packageParams);
        packageParams.put("sign",signature);

        System.out.println(packageParams);



    }


}