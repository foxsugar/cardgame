package com.code.server.cardgame.grpc;

import com.code.server.cardgame.message.MessageHolder;
import com.code.server.cardgame.rpc.GameRpcHandler;

/**
 * Created by sunxianping on 2017/3/31.
 */
public class GRpcMsgDispatch {

    public static void dispatch(MessageHolder messageHolder){
        switch (messageHolder.rpcHolder.rpcMethod) {
            case "charge":
                GameRpcImpl.doCharge(messageHolder);
                break;
            case "exchange":
//                GameRpcImpl.doExchange(messageHolder);
                break;
        }
    }


}
