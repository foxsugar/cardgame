package com.code.server.cardgame.handler;

import com.code.server.cardgame.Message.MessageHolder;
import com.code.server.cardgame.core.GameManager;
import com.code.server.cardgame.core.MsgDispatch;
import com.code.server.cardgame.core.Player;
import com.code.server.cardgame.utils.JsonUtil;
import com.google.gson.Gson;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import net.sf.json.JSONObject;

/**
 * Created by win7 on 2017/3/9.
 */
public class GameMsgHandler extends ChannelDuplexHandler {


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg){
        MessageHolder messageHolder = new MessageHolder();
        messageHolder.ctx = ctx;
        messageHolder.message = msg;
        //加进队列
        GameProcessor.getInstance().messageQueue.add(messageHolder);
    }


    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        super.write(ctx, msg, promise);
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //可从内存中剔除的玩家
        Player player = GameManager.getPlayerByCtx(ctx);
        if (player != null) {
            GameManager.getInstance().getKickUser().put(player.getUserId(), player);
        }
        super.channelInactive(ctx);
    }


}
