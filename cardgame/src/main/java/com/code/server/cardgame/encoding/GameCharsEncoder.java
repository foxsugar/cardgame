package com.code.server.cardgame.encoding;

import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.sf.json.JSONObject;

public class GameCharsEncoder extends MessageToByteEncoder<Object> {
    
    private Gson gson = new Gson();

    protected void encode(ChannelHandlerContext ctx, Object object, ByteBuf out) throws Exception {
//        byte[] data = json.toString().getBytes("utf-8");
        String json = gson.toJson(object);
        byte[] data = json.getBytes("utf-8");
        System.out.println("发送消息=== "+json);
        out.writeInt(data.length);
        out.writeBytes(data);
    }
    
//    @Override
//    protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        DataOutputStream oos = new DataOutputStream(bos);
//        //oos.writeObject(msg);
//        //oos.writeUTF(((JSONObject)msg).toString());
//        oos.write(msg.toString().getBytes("utf-8"));
//        oos.flush();
//        byte[] bytes = bos.toByteArray();
//
//        ChannelBuffer buf = ChannelBuffers.buffer(4 + bytes.length);
//        buf.writeInt(bytes.length);
//        buf.writeBytes(bytes);
//
//
//        return buf;
//    }
}
