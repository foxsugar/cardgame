package com.code.server.cardgame.core;

import io.netty.channel.ChannelHandlerContext;

import java.util.*;

/**
 * Created by sun on 2015/8/26.
 */
public class GameManager {

    public static GameManager instance;

    public Map<Long, Player> players = new HashMap<>();
    public Map<Long, ChannelHandlerContext> ctxs = new HashMap<>();
    public Map<Long,String> id_nameMap = new HashMap<>();
    public Map<String, Long> name_idMap = new HashMap<>();
    public Set<ChannelHandlerContext> ctxSet = new HashSet<>();



    private GameManager() {

    }

    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }



//    public void sendMsg2Client(ChannelHandlerContext ctx, PB.S2CMessage msg) {
//        BinaryWebSocketFrame bwf = new BinaryWebSocketFrame();
//        int length = msg.getSerializedSize();
//        bwf.content().writeInt(length);
//        bwf.content().writeBytes(msg.toByteArray());
//        ctx.writeAndFlush(bwf);
//    }
}
