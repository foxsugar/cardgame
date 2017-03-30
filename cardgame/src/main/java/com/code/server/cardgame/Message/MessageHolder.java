package com.code.server.cardgame.Message;

import io.netty.channel.ChannelHandlerContext;
import org.apache.thrift.async.AsyncMethodCallback;

/**
 * Created by sun on 2015/8/25.
 */
public class MessageHolder<T> {
    public static final int MSG_TYPE_CLIENT_JSON = 0;
    public static final int MSG_TYPE_RPC = 1;
    public int msgType;
    public Object message;
    public ChannelHandlerContext ctx;
    public AsyncMethodCallback<T> rpcCallback;
}
