package com.code.server.cardgame.core;


import com.code.server.db.model.User;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by win7 on 2017/3/9.
 */
public class Player {
    private long userId;
    private User user;
    private ChannelHandlerContext ctx;

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
}
