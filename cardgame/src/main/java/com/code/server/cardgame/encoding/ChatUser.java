package com.code.server.cardgame.encoding;

import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by win7 on 2017/2/13.
 */
public class ChatUser {

    private static ChatUser instance;
    public static ChatUser getInstance(){
        if (instance == null) {
            instance = new ChatUser();
        }
        return instance;
    }
    public Map<Integer, ChannelHandlerContext> users = new HashMap<>();

}
