package com.code.server.cardgame.rpc;

import com.code.server.cardgame.message.MessageHolder;

/**
 * Created by sunxianping on 2017/3/31.
 */
public class ThriftMsgDispatch {

    public static void dispatch(MessageHolder messageHolder){
        switch (messageHolder.rpcHolder.rpcMethod) {
            case "charge":
                GameRpcHandler.doCharge(messageHolder);
                break;
            case "exchange":
                GameRpcHandler.doExchange(messageHolder);
                break;
        }
    }


}
