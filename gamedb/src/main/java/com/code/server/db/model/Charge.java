package com.code.server.db.model;

/**
 * Created by Administrator on 2017/5/23.
 */
public class Charge {

        private String orderid;
        private String userid;
        private Integer money;
        private Integer origin;
        private String status;
        private String sign;
        private String sp_ip;
        private String shareid;
        private String share_content;
        private String share_area;
        private Integer money_point;
        private String username;
        private String recharge_source;


    public String getOrderid() {
        return orderid;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public Integer getMoney() {
        return money;
    }

    public void setMoney(Integer money) {
        this.money = money;
    }

    public Integer getOrigin() {
        return origin;
    }

    public void setOrigin(Integer origin) {
        this.origin = origin;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getSp_ip() {
        return sp_ip;
    }

    public void setSp_ip(String sp_ip) {
        this.sp_ip = sp_ip;
    }

    public String getShareid() {
        return shareid;
    }

    public void setShareid(String shareid) {
        this.shareid = shareid;
    }

    public String getShare_content() {
        return share_content;
    }

    public void setShare_content(String share_content) {
        this.share_content = share_content;
    }

    public String getShare_area() {
        return share_area;
    }

    public void setShare_area(String share_area) {
        this.share_area = share_area;
    }

    public Integer getMoney_point() {
        return money_point;
    }

    public void setMoney_point(Integer money_point) {
        this.money_point = money_point;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRecharge_source() {
        return recharge_source;
    }

    public void setRecharge_source(String recharge_source) {
        this.recharge_source = recharge_source;
    }
}
