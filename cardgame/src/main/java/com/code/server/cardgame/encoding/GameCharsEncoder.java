package com.code.server.cardgame.encoding;

import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class GameCharsEncoder extends MessageToByteEncoder<Object> {
    
    private Gson gson = new Gson();

    protected void encode(ChannelHandlerContext ctx, Object object, ByteBuf out) throws Exception {
        String json = gson.toJson(object);
        byte[] data = json.getBytes("utf-8");
        System.out.println("发送消息===  "+json);
        out.writeInt(data.length);
        out.writeBytes(data);
    }
}
