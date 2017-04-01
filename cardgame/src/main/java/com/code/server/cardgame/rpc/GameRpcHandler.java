package com.code.server.cardgame.rpc;

import com.code.server.cardgame.Message.MessageHolder;
import com.code.server.cardgame.core.GameManager;
import com.code.server.cardgame.core.Player;
import com.code.server.cardgame.handler.GameProcessor;
import com.code.server.cardgame.utils.SpringUtil;
import com.code.server.cardgame.utils.ThreadPool;
import com.code.server.db.Service.UserService;
import com.code.server.db.model.User;
import com.code.server.rpc.idl.GameRPC;
import com.code.server.rpc.idl.Order;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;

/**
 * Created by sunxianping on 2017/3/29.
 */
public class GameRpcHandler implements GameRPC.AsyncIface {



    @Override
    public void charge(Order order, AsyncMethodCallback<Integer> resultHandler) throws TException {
        MessageHolder<Integer> messageHolder = new MessageHolder<>();
        messageHolder.msgType = MessageHolder.MSG_TYPE_RPC;
        messageHolder.message = order;

        MessageHolder.RpcHolder<Integer> rpcHolder = new MessageHolder.RpcHolder<>();
        rpcHolder.rpcCallback = resultHandler;
        rpcHolder.rpcMethod = "charge";
        messageHolder.rpcHolder = rpcHolder;
        //加进队列
        GameProcessor.getInstance().messageQueue.add(messageHolder);
        resultHandler.onComplete(1);
    }

    @Override
    public void getUserInfo(long userId, AsyncMethodCallback<com.code.server.rpc.idl.User> resultHandler) throws TException {

    }

    @Override
    public void exchange(Order order, AsyncMethodCallback<Integer> resultHandler) throws TException {

    }


    public static void doCharge(MessageHolder messageHolder){
        Order order = (Order) messageHolder.message;
        //玩家是否在内存中
        Player player = GameManager.getInstance().getPlayers().get(order.getUserId());
        if (player != null) {
            User user = player.getUser();
            double newMoney = user.getMoney() + order.getMoney();
            user.setMoney(newMoney);
        } else {
            //不在内存中
            ThreadPool.getInstance().executor.execute(()->{
                UserService userService = SpringUtil.getBean(UserService.class);
                User user = userService.getUserByUserId(order.getUserId());
                double newMoney = user.getMoney() + order.getMoney();
                user.setMoney(newMoney);
                userService.save(user);
            });
        }
    }

    public static void doExchange(MessageHolder messageHolder){
        Order order = (Order) messageHolder.message;
        Player player = GameManager.getInstance().getPlayers().get(order.getUserId());
        if (player != null) {
            User user = player.getUser();
            double newMoney = user.getMoney() + order.getMoney();
            user.setMoney(newMoney);
        } else {
            //不在内存中
            ThreadPool.getInstance().executor.execute(()->{
                UserService userService = SpringUtil.getBean(UserService.class);
                User user = userService.getUserByUserId(order.getUserId());
                double newMoney = user.getMoney() + order.getMoney();
                user.setMoney(newMoney);
                userService.save(user);
            });
        }
    }

}
