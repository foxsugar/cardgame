package com.code.server.login.business;

/**
 * Created by Administrator on 2017/4/12.
 */
public class WeixinPayConstants {

    public static final String appid = "wxad87bc7722faff71";//在微信开发平台登记的app应用
    public static final String appsecret = "XXXXXXXXX";
    public static final String mch_id = "1458783202";//商户号
    public static final String partnerkey ="XXXXXXXXXXXXXXXXXXXXXXXXXXXXX";//不是商户登录密码，是商户在微信平台设置的32位长度的api秘钥，
    public static final String createOrderURL="https://api.mch.weixin.qq.com/pay/unifiedorder";
    public static final String backUri="http://XXXXXXXX/api/weixin/topay.jhtml";//微信支付下单地址
    public static final String notify_url="http://XXXXXXXXXX/api/weixin/notify.jhtml";//异步通知地址

}
