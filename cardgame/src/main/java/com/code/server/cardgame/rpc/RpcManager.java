package com.code.server.cardgame.rpc;

import com.code.server.cardgame.config.ServerConfig;
import com.code.server.cardgame.timer.GameTimer;
import com.code.server.cardgame.timer.TimerNode;
import com.code.server.cardgame.utils.SpringUtil;
import com.code.server.cardgame.utils.ThreadPool;
import com.code.server.rpc.client.AdminRpcClient;
import com.code.server.rpc.client.GameRpcClient;
import com.code.server.rpc.client.TransportManager;
import com.code.server.rpc.idl.AdminRPC;
import com.code.server.rpc.idl.GameRPC;
import com.code.server.rpc.idl.Order;
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

    //    private TTransport adminTransport;
    private AdminRPC.Client adminRpcClient;

    private GameRPC.Client gameRpcClient;

    public TServer gameRpcServer;

    private List<Rebate> failedRebate = new CopyOnWriteArrayList<>();

    private RpcManager() {
    }

    public static RpcManager getInstance() {
        if (instance == null) {
            instance = new RpcManager();
        }
        return instance;
    }


    private TTransport getTransport() throws TTransportException {
        return TransportManager.getTransport(serverConfig.getAdminRpcHost().trim(), serverConfig.getAdminRpcPort());
    }


    public static void main(String[] args) {

        for (int j = 0; j < 10; j++) {

            new Thread(new Runnable() {
                @Override
                public void run() {
//                    testAdmin();
                    testGame();
                }
            }).start();


        }

    }

    private static void testAdmin() {

        for (int i = 0; i < 1000; i++) {

//                System.out.println(i);

            TTransport adminTransport = null;
            try {
                adminTransport = TransportManager.getTransport("192.168.1.150", 9999);
            } catch (TTransportException e) {
                e.printStackTrace();
            }
//            TTransport adminTransport = TransportManager.getTransport("127.0.0.1",8090);
            AdminRPC.Client adminRpcClient = null;
            try {
                adminRpcClient = AdminRpcClient.getAClient(adminTransport);
            } catch (TTransportException e) {
                e.printStackTrace();
            }
            List<Rebate> list = new ArrayList<>();
            Rebate rebate = new Rebate();
            rebate.setId(1L);
            rebate.setUserId(1L);
            rebate.setTime(2L);
            rebate.setIsHasReferee(true);
            rebate.setRefereeId(1);
            list.add(rebate);
            try {
                adminRpcClient.rebate(list);
            } catch (TException e) {
                e.printStackTrace();
            }
            adminTransport.close();
        }
    }

    private static void testGame() {
        for (int i = 0; i < 1000; i++) {
            try {
                TTransport adminTransport = TransportManager.getTransport("192.168.1.132", 9090);

                GameRPC.Client client = GameRpcClient.getAClient(adminTransport);
                Order order = new Order();
                order.setUserId(1);
                order.setNum(1);
                client.charge(order);

            } catch (Exception e) {

            }
        }

    }


    public void sendRpcRebat(List<Rebate> rebates) {
        try {
            TTransport tTransport = getTransport();
            AdminRPC.Client adminRpcClient = AdminRpcClient.getAClient(tTransport);
            adminRpcClient.rebate(rebates);
            tTransport.close();
        } catch (TException e) {
            logger.error("send rpc rebat error ", e);
            //todo 发送不成功处理
//            failedRebate.addAll(rebates);
        }
    }

    public boolean referrerIsExist(long referrer) {
        boolean result = false;
        try {
            TTransport tTransport = getTransport();
            AdminRPC.Client adminRpcClient = AdminRpcClient.getAClient(tTransport);
            result = adminRpcClient.isExist(referrer);
            tTransport.close();
        } catch (TException e) {
            logger.error("send rpc rebat error ", e);
        }
        return result;
    }


    public void startGameRpcServer() throws TTransportException {
        gameRpcServer = GameRpcServer.StartServer(serverConfig.getGameRpcServerPort(), new GameRpcHandler());
    }

    public void checkGameRpcServerWork() {
        long time = System.currentTimeMillis();
        GameTimer.getInstance().addTimerNode(new TimerNode(time, 1000L * 5, true, () -> {
            if (RpcManager.getInstance().gameRpcServer != null && !RpcManager.getInstance().gameRpcServer.isServing()) {
                RpcManager.getInstance().gameRpcServer.stop();
                RpcManager.getInstance().gameRpcServer = null;
                ThreadPool.getInstance().executor.execute(() -> {
                    try {
                        RpcManager.getInstance().startGameRpcServer();
                    } catch (TTransportException e) {
                        e.printStackTrace();
                    }
                });
            }
        }));
    }


}
