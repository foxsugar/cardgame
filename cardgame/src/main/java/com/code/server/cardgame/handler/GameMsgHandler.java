package com.code.server.cardgame.handler;

import com.code.server.cardgame.Message.MessageHolder;
import com.code.server.cardgame.utils.JsonUtil;
import com.google.gson.Gson;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.sf.json.JSONObject;

/**
 * Created by win7 on 2017/3/9.
 */
public class GameMsgHandler extends ChannelInboundHandlerAdapter {

    private Gson gson = new Gson();


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg){
        MessageHolder messageHolder = new MessageHolder();
        messageHolder.ctx = ctx;
        messageHolder.message = msg;

        JSONObject jSONObject = (JSONObject)msg;
        String service = jSONObject.getString("service");
        String method = jSONObject.getString("method");
        JSONObject params = jSONObject.getJSONObject("params");

        //加进队列
        GameProcessor.getInstance().messageQueue.add(messageHolder);
    }

    public static void main(String[] args) {
    }
}
