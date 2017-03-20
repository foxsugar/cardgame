package com.code.server.cardgame.core;


import com.code.server.cardgame.response.ResponseVo;
import com.code.server.db.model.User;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

/**
 * Created by win7 on 2017/3/9.
 */
public class Player {
    private long userId;
    private User user;
    private ChannelHandlerContext ctx;
    private long lastSendMsgTime;

    public void sendMsg(Object msg){
        this.ctx.writeAndFlush(msg);
    }

    public void sendMsg(String service, String method, Object msg) {
        sendMsg(new ResponseVo(service,method,msg));
    }

    public static void sendMsg2Player(Object msg, long userId) {
        Player other = GameManager.getInstance().players.get(userId);
        if (other != null) {
            other.ctx.writeAndFlush(msg);
        }
    }

    public static void sendMsg2Player(Object msg, List<Long> users) {
        for (long id : users) {
            sendMsg2Player(msg,id);
        }
    }

    public static void sendMsg2Player(String service, String method, Object msg, List<Long> users) {
        sendMsg2Player(new ResponseVo(service,method,msg),users);
    }

    public static void sendMsg2Player(String service, String method, Object msg, long userId) {
        sendMsg2Player(new ResponseVo(service,method,msg),userId);
    }





    public long getUserId() {
        return userId;
    }

    public Player setUserId(long userId) {
        this.userId = userId;
        return this;
    }

    public User getUser() {
        return user;
    }

    public Player setUser(User user) {
        this.user = user;
        return this;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public Player setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
        return this;
    }



    public long getLastSendMsgTime() {
        return lastSendMsgTime;
    }

    public Player setLastSendMsgTime(long lastSendMsgTime) {
        this.lastSendMsgTime = lastSendMsgTime;
        return this;
    }
}
