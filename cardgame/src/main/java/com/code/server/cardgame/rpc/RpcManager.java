package com.code.server.cardgame.rpc;

import com.code.server.cardgame.config.ServerConfig;
import com.code.server.rpc.client.AdminRpcClient;
import com.code.server.rpc.client.TransportManager;
import com.code.server.rpc.idl.GameRPC;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

/**
 * Created by sunxianping on 2017/3/30.
 */
public class RpcManager {
    private static RpcManager instance;
    private ServerConfig serverConfig;

    private TTransport adminTransport;
    private GameRPC.Client gameRpcClient;



    private void getTransport() throws TTransportException {
        adminTransport = TransportManager.getTransport(serverConfig.getAdminRpcHost(),serverConfig.getPort());
    }

    private RpcManager(){}
    public static RpcManager getInstance(){
        if (instance == null) {
            instance = new RpcManager();
        }
        return instance;
    }


}
