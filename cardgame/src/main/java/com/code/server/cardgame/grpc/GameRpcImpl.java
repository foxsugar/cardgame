package com.code.server.cardgame.grpc;

import com.code.server.cardgame.core.GameManager;
import com.code.server.cardgame.core.Player;
import com.code.server.cardgame.handler.GameProcessor;
import com.code.server.cardgame.handler.MessageHolder;
import com.code.server.cardgame.utils.SpringUtil;
import com.code.server.cardgame.utils.ThreadPool;
import com.code.server.db.Service.UserService;
import com.code.server.grpc.idl.*;
import com.code.server.grpc.idl.Order;
import com.code.server.grpc.idl.User;
import com.code.server.rpc.idl.ChargeType;
import io.grpc.stub.StreamObserver;

/**
 * Created by sunxianping on 2017/4/27.
 */
public class GameRpcImpl extends GameServiceGrpc.GameServiceImplBase {

    @Override
    public void charge(Order request, StreamObserver<Response> responseObserver) {
        MessageHolder<Response> messageHolder = new MessageHolder<>();
        messageHolder.msgType = MessageHolder.MSG_TYPE_THRIFT;
        messageHolder.message = request;

        MessageHolder.RpcHolder<Response> rpcHolder = new MessageHolder.RpcHolder<>();
        rpcHolder.grpcCallback = responseObserver;
        rpcHolder.rpcMethod = "charge";
        messageHolder.rpcHolder = rpcHolder;
        //加进队列
        GameProcessor.getInstance().messageQueue.add(messageHolder);
    }

    @Override
    public void getUserInfo(Request request, StreamObserver<User> responseObserver) {
        super.getUserInfo(request, responseObserver);
    }

    @Override
    public void exchange(Order request, StreamObserver<Response> responseObserver) {
        super.exchange(request, responseObserver);
    }




    public static void doCharge(MessageHolder<Response> messageHolder){
        Order order = (Order) messageHolder.message;
        //玩家是否在内存中
        Player player = GameManager.getInstance().getPlayers().get(order.getUserId());
        if (player != null) {
            chargeLogic(player.getUser(), order, messageHolder,true);

        } else {
            //不在内存中
            ThreadPool.getInstance().executor.execute(()->{
                UserService userService = SpringUtil.getBean(UserService.class);
                com.code.server.db.model.User user = userService.getUserByUserId(order.getUserId());
                chargeLogic(user, order, messageHolder,false);
            });
        }
    }

    private static void chargeLogic(com.code.server.db.model.User user, Order order, MessageHolder<Response> messageHolder, boolean isAsyncSave){
        if (user == null) {
            messageHolder.rpcHolder.grpcCallback.onNext(Response.newBuilder().build());
            messageHolder.rpcHolder.grpcCallback.onCompleted();
            return;
        }
        if (order.getType() == ChargeType.money.getValue()) {
            double newMoney = user.getMoney() + order.getNum();
            user.setMoney(newMoney);
        } else if(order.getType() == ChargeType.gold.getValue()) {
            double newGold = user.getGold() + order.getNum();
            user.setGold(newGold);
        }
        if (isAsyncSave) {
            saveUser(user);
        } else {
            UserService userService = SpringUtil.getBean(UserService.class);
            userService.save(user);
        }

        messageHolder.rpcHolder.grpcCallback.onCompleted();
    }

    private static void saveUser(com.code.server.db.model.User user){
        if (user == null) {
            return;
        }
        ThreadPool.getInstance().executor.execute(()->{
            UserService userService = SpringUtil.getBean(UserService.class);
            userService.save(user);
        });
    }
}
