package com.code.server.cardgame.rpc;

import com.code.server.cardgame.Message.MessageHolder;
import com.code.server.cardgame.handler.GameProcessor;
import com.code.server.rpc.idl.GameRPC;
import com.code.server.rpc.idl.Order;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;

/**
 * Created by sunxianping on 2017/3/29.
 */
public class GameRpcHandler implements GameRPC.AsyncIface {



    @Override
    public void charge(Order order, AsyncMethodCallback<Integer> resultHandler) throws TException {
        MessageHolder<Integer> messageHolder = new MessageHolder<>();
        messageHolder.msgType = MessageHolder.MSG_TYPE_RPC;
        messageHolder.message = order;
        messageHolder.rpcCallback = resultHandler;
        //加进队列
        GameProcessor.getInstance().messageQueue.add(messageHolder);
        resultHandler.onComplete(1);
    }


}
