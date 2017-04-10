package com.code.server.cardgame.rpc;

import com.code.server.cardgame.config.ServerConfig;
import com.code.server.cardgame.timer.GameTimer;
import com.code.server.cardgame.timer.ITimeHandler;
import com.code.server.cardgame.timer.TimerNode;
import com.code.server.cardgame.utils.SpringUtil;
import com.code.server.cardgame.utils.ThreadPool;
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

//    private TTransport adminTransport;
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



    private TTransport getTransport() throws TTransportException {
        return TransportManager.getTransport(serverConfig.getAdminRpcHost().trim(),serverConfig.getAdminRpcPort());
    }


    public static void main(String[] args) {
        try {

//            AdminRPC.Client adminRpcClient = getAdminClient();
//            adminRpcClient.test();




//            1: i64 id,
//            2:i64 userId,
//            3:i32 refereeId,
//            4:double rebateNum,
//            5:i64 time,
//            6:bool isHasReferee,
            long start = System.currentTimeMillis();
            for(int i=0;i<1000;i++){
                TTransport adminTransport = TransportManager.getTransport("192.168.1.150",10000);
//            TTransport adminTransport = TransportManager.getTransport("127.0.0.1",8090);
                AdminRPC.Client adminRpcClient = AdminRpcClient.getAClient(adminTransport);
                List<Rebate> list = new ArrayList<>();
                Rebate rebate = new Rebate();
                rebate.setId(1L);
                rebate.setUserId(1L);
                rebate.setTime(2L);
                rebate.setIsHasReferee(true);
                rebate.setRefereeId(1);
                list.add(rebate);
                adminRpcClient.rebate(list);
                adminTransport.close();
//                System.out.println(i);
            }
            long end = System.currentTimeMillis();
            System.out.println("耗时 : "+ (end - start));

        } catch (TTransportException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }
    }



    public void sendRpcRebat(List<Rebate> rebates){
        try {
            TTransport tTransport = getTransport();
            AdminRPC.Client adminRpcClient = AdminRpcClient.getAClient(tTransport);
            adminRpcClient.rebate(rebates);
            tTransport.close();
        } catch (TException e) {
            logger.error("send rpc rebat error ",e);
            //todo 发送不成功处理
//            failedRebate.addAll(rebates);
        }
    }


    public void startGameRpcServer() throws TTransportException {
        gameRpcServer = GameRpcServer.StartServer(serverConfig.getGameRpcServerPort(),new GameRpcHandler());
    }

    public void checkGameRpcServerWork(){
        long time = System.currentTimeMillis();
        GameTimer.getInstance().addTimerNode(new TimerNode(time, 1000L * 5, true, ()-> {
            if(RpcManager.getInstance().gameRpcServer!=null && !RpcManager.getInstance().gameRpcServer.isServing()){
                RpcManager.getInstance().gameRpcServer.stop();
                RpcManager.getInstance().gameRpcServer = null;
                ThreadPool.getInstance().executor.execute(()->{
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
