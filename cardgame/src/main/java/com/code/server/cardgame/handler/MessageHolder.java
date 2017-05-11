package com.code.server.cardgame.handler;

import com.code.server.grpc.idl.Response;
import io.grpc.stub.StreamObserver;
import io.netty.channel.ChannelHandlerContext;
import org.apache.thrift.async.AsyncMethodCallback;

/**
 * Created by sun on 2015/8/25.
 */
public class MessageHolder<T> {
    public static final int MSG_TYPE_CLIENT_JSON = 0;
    public static final int MSG_TYPE_THRIFT = 1;
    public static final int MSG_TYPE_GRPC = 2;
    public int msgType;
    public Object message;
    public ChannelHandlerContext ctx;
    public RpcHolder<T> rpcHolder;

    public static class RpcHolder<T>{
        public String rpcMethod;
        public AsyncMethodCallback<T> thriftCallback;
        public StreamObserver<T> grpcCallback;
    }
}

