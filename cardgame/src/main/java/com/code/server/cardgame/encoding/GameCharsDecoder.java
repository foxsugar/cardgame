package com.code.server.cardgame.encoding;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class GameCharsDecoder extends ByteToMessageDecoder {

    @SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(GameCharsDecoder.class);

    private static final int HEAD_SIZE = 4;
    private static final int DATA_SIZE_MIN = 2;
        


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> list) throws Exception {
        if (buf.readableBytes() < HEAD_SIZE) {
            return;
        }

        //标记
        buf.markReaderIndex();
        //读取长度
        int dataLength = buf.readInt();

        //判断长度合法
        if (dataLength < DATA_SIZE_MIN) {
            logger.error("data length error:" + dataLength);
            ctx.close();
            return;
        }

        //数据不足
        if (buf.readableBytes() < dataLength){
            //重置标记
            buf.resetReaderIndex();
            return;
        }

        //读取
        byte[] data = new byte[dataLength];
        buf.readBytes(data);

        String str = new String(data, "utf-8");
        JSONObject json = JSONObject.fromObject(str);

        list.add(json);
    }
//
//	@Override
//    protected  Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
//		List<JSONObject> datas = new LinkedList<>();
//		ChannelBuffer buf = (ChannelBuffer) msg;
//
//		while(true){
//			if(buf.readableBytes() < 4){
//				System.err.println("readable:" + buf.readableBytes());
//				break;
//			}
//
//			int len = buf.getInt(buf.readerIndex());
//			if(buf.readableBytes() < len + 4){
//				System.err.println("readable:" + buf.readableBytes());
//				break;
//			}
//
//			//skip
//			buf.skipBytes(4);
//
//			//读取数据
//			byte[] temp = new byte[len];
//			buf.readBytes(temp);
//
//			String str = new String(temp, "utf-8");
//			JSONObject json = JSONObject.fromObject(str);
//
//			datas.add(json);
//		}
//
//		return datas;
//
//    }

    @SuppressWarnings("unused")
	private static byte[] uncompress(byte[] b) {
        if (b == null)
            return null;
        String map;
        byte bt;
        ArrayList<Byte> buffer = new ArrayList<Byte>();
        char client = 0;
        int readCount = 0;
        while (readCount != b.length) {
            map = Integer.toBinaryString((int) b[readCount] + 128);
            readCount++;
            bt = b[readCount];
            readCount++;
            for (int i = 0; i < map.length(); ++i) {
                client = map.charAt(i);
                if (client == '1')
                    buffer.add(i, bt);
            }
        }
        byte[] out = new byte[buffer.size()];
        for (int j = 0; j < out.length; ++j)
            out[j] = buffer.get(j);
        return out;
    }

}
