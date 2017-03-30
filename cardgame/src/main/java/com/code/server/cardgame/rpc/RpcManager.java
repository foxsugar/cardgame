package com.code.server.cardgame.rpc;

import com.code.server.cardgame.config.ServerConfig;
import com.code.server.cardgame.core.GameManager;
import com.code.server.db.model.User;
import com.code.server.rpc.client.AdminRpcClient;
import com.code.server.rpc.client.TransportManager;
import com.code.server.rpc.idl.AdminRPC;
import com.code.server.rpc.idl.GameRPC;
import com.code.server.rpc.idl.Rebate;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by sunxianping on 2017/3/30.
 */
public class RpcManager {
    private final Logger logger = LoggerFactory.getLogger(RpcManager.class);
    private static RpcManager instance;
    private ServerConfig serverConfig;

    private TTransport adminTransport;
    private AdminRPC.Client adminRpcClient;

    private GameRPC.Client gameRpcClient;

    private List<Rebate> failedRebate = new CopyOnWriteArrayList<>();

    private RpcManager(){}
    public static RpcManager getInstance(){
        if (instance == null) {
            instance = new RpcManager();
        }
        return instance;
    }

    public AdminRPC.Client getAdminClient() throws TTransportException {
        if (adminTransport == null || !adminTransport.isOpen()) {
            getTransport();
        }
        if (adminRpcClient == null) {
            adminRpcClient = AdminRpcClient.getAClient(adminTransport);
        }
        return adminRpcClient;
    }

    private void getTransport() throws TTransportException {
        adminTransport = TransportManager.getTransport(serverConfig.getAdminRpcHost(),serverConfig.getPort());
    }





    public void sendRpcRebat(List<Rebate> rebates){
        try {
            RpcManager.getInstance().getAdminClient().rebate(rebates);
        } catch (TException e) {
            logger.error("send rpc rebat error ");
            failedRebate.addAll(rebates);
        }
    }



}
