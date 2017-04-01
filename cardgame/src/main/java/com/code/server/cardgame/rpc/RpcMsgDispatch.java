package com.code.server.cardgame.rpc;

import com.code.server.cardgame.Message.MessageHolder;
import com.code.server.cardgame.core.MsgDispatch;

/**
 * Created by sunxianping on 2017/3/31.
 */
public class RpcMsgDispatch {

    public static void dispatch(MessageHolder messageHolder){
        switch (messageHolder.rpcHolder.rpcMethod) {
            case "charge":
                GameRpcHandler.doCharge(messageHolder);
                break;

        }
    }


}
