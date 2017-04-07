package com.code.server.cardgame.rpc;

import com.code.server.cardgame.config.ServerConfig;
import com.code.server.cardgame.core.GameManager;
import com.code.server.cardgame.utils.SpringUtil;
import com.code.server.db.model.User;
import com.code.server.rpc.client.AdminRpcClient;
import com.code.server.rpc.client.TransportManager;
import com.code.server.rpc.idl.AdminRPC;
import com.code.server.rpc.idl.GameRPC;
import com.code.server.rpc.idl.Rebate;
import com.code.server.rpc.server.GameRpcServer;
import org.apache.thrift.TException;
import org.apache.thrift.server.TServer;
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
    private ServerConfig serverConfig = SpringUtil.getBean(ServerConfig.class);

    private TTransport adminTransport;
    private AdminRPC.Client adminRpcClient;

    private GameRPC.Client gameRpcClient;

    public TServer gameRpcServer;

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
        adminTransport = TransportManager.getTransport(serverConfig.getAdminRpcHost().trim(),serverConfig.getAdminRpcPort());
    }


    public static void main(String[] args) {
        try {
            TTransport adminTransport = TransportManager.getTransport("192.168.1.150",10000);
//            TTransport adminTransport = TransportManager.getTransport("127.0.0.1",8090);
            AdminRPC.Client adminRpcClient = AdminRpcClient.getAClient(adminTransport);
//            AdminRPC.Client adminRpcClient = getAdminClient();
//            adminRpcClient.test();
            List<Rebate> list = new ArrayList<>();
            Rebate rebate = new Rebate();
            rebate.setId(1L);
            rebate.setUserId(1L);
            rebate.setTime(2L);
            rebate.setIsHasReferee(true);
            rebate.setRefereeId(1);



//            1: i64 id,
//            2:i64 userId,
//            3:i32 refereeId,
//            4:double rebateNum,
//            5:i64 time,
//            6:bool isHasReferee,
            list.add(rebate);
            adminRpcClient.rebate(list);

        } catch (TTransportException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }
    }



    public void sendRpcRebat(List<Rebate> rebates){
        try {
            RpcManager.getInstance().getAdminClient().rebate(rebates);
        } catch (TException e) {
            logger.error("send rpc rebat error ",e);
            //todo 发送不成功处理
//            failedRebate.addAll(rebates);
        }
    }


    public void startGameRpcServer() throws TTransportException {
        gameRpcServer = GameRpcServer.StartServer(serverConfig.getGameRpcServerPort(),new GameRpcHandler());
    }


}
