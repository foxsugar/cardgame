package com.code.server.rpc.server;

import com.code.server.rpc.idl.GameRPC;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;

/**
 * Created by sunxianping on 2017/3/29.
 */
public class ServerHandler implements GameRPC.AsyncIface {



    @Override
    public void charge(long userId, int money, AsyncMethodCallback<Integer> resultHandler) throws TException {
        System.out.println("=====================");
        resultHandler.onComplete(1);
    }


}
