package com.code.server.cardgame.core;

import com.code.server.cardgame.core.doudizhu.CardStruct;
import com.code.server.cardgame.core.doudizhu.GameDouDiZhu;
import com.code.server.cardgame.core.doudizhu.RoomDouDiZhu;
import com.code.server.cardgame.core.service.GameChatService;
import com.code.server.cardgame.core.service.GameUserService;
import com.code.server.cardgame.core.tiandakeng.GameTianDaKeng;
import com.code.server.cardgame.core.tiandakeng.RoomTanDaKeng;
import com.code.server.cardgame.grpc.GRpcMsgDispatch;
import com.code.server.cardgame.handler.MessageHolder;
import com.code.server.cardgame.playdice.ErrorCodeDice;
import com.code.server.cardgame.playdice.GameDice;
import com.code.server.cardgame.playdice.RoomDice;
import com.code.server.cardgame.response.ErrorCode;
import com.code.server.cardgame.response.ResponseVo;
import com.code.server.cardgame.rpc.ThriftMsgDispatch;
import com.code.server.cardgame.utils.SpringUtil;
import com.google.gson.Gson;
import io.netty.channel.ChannelHandlerContext;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sun on 2015/8/21.
 */

@Service
public class MsgDispatch {

    private final Logger logger = LoggerFactory.getLogger(MsgDispatch.class);

    private Gson gson = new Gson();

    public static void sendMsg(ChannelHandlerContext ctx, Object msg) {
        if(ctx != null){
            ctx.writeAndFlush(msg);
        }
    }


    public void handleMessage(MessageHolder msgHolder) {

        Object message = msgHolder.message;
        switch (msgHolder.msgType) {
            case MessageHolder.MSG_TYPE_THRIFT:{
                ThriftMsgDispatch.dispatch(msgHolder);
                break;
            }
            case MessageHolder.MSG_TYPE_GRPC:{
                GRpcMsgDispatch.dispatch(msgHolder);
                break;
            }
            case MessageHolder.MSG_TYPE_CLIENT_JSON:{
                JSONObject jSONObject = (JSONObject) message;
                logger.info("handle message== " + jSONObject);
                String service = jSONObject.getString("service");
                String method = jSONObject.getString("method");
                JSONObject params = jSONObject.getJSONObject("params");
                //逻辑
                int code = dispatchAllMsg(service, method, params, msgHolder.ctx);
                //客户端要的方法返回
                if (code != 0) {
                    ResponseVo vo = new ResponseVo(service, method, code);
                    sendMsg(msgHolder.ctx, vo);
                }
                break;
            }
            case MessageHolder.MSG_TYPE_INNER:{
                JSONObject jSONObject = (JSONObject) message;
                String service = jSONObject.getString("service");
                String method = jSONObject.getString("method");
                JSONObject params = jSONObject.getJSONObject("params");
                if("gameService".equals(service) || "roomService".equals(service) ){
                    Player player = GameManager.getInstance().getPlayers().get(msgHolder.userId);
                    if(player != null){
                        int code = dispatchGameService(method, params, player);
                        if (code != 0) {
                            ResponseVo vo = new ResponseVo(service, method, code);
                            sendMsg(msgHolder.ctx, vo);
                        }
                    }
                }
             break;
            }

        }


    }

    private void handleRpcMessage(){

    }

    private int dispatchAllMsg(String service, String method, JSONObject params, ChannelHandlerContext ctx) {
        switch (service) {
            case "userService":
                return dispatchUserService(method, params, ctx);
            case "roomService":
                return dispatchRoomService(method, params, ctx);
            case "gameService":{

                Player player = GameManager.getPlayerByCtx(ctx);
                if (player == null) {
                    return -1;
                }
                return dispatchGameService(method, params, player);
            }
            case "chatService":
                return dispatchChatService(method, params, ctx);

            case "gameTDKService":{

                Player player = GameManager.getPlayerByCtx(ctx);
                if (player == null) {
                    return -1;
                }
                return dispatchGameService(method, params, player);
            }
            default:
                return ErrorCode.REQUEST_PARAM_ERROR;
        }
    }

    private int dispatchUserService(String method, JSONObject params, ChannelHandlerContext ctx) {

        GameUserService gameUserService = SpringUtil.getBean(GameUserService.class);
        switch (method) {
            case "login":
                String account = params.getString("account");
                String password = params.getString("password");
                return gameUserService.login(account, password, ctx);
            case "appleCheck":
                return gameUserService.appleCheck(ctx);
            case "checkOpenId":
                String openId = params.getString("openId");
                String username = params.getString("username");
                String image = params.getString("image");
                int sex = Integer.parseInt(params.getString("sex"));
                return gameUserService.checkOpenId(openId, username, image, sex, ctx);
            case "getUserMessage": {
                Player player = GameManager.getPlayerByCtx(ctx);
                if (player == null) {
                    return ErrorCode.YOU_HAVE_NOT_LOGIN;
                }
                return gameUserService.getUserMessage(player);
            }
            case "reconnection": {
                Player player = GameManager.getPlayerByCtx(ctx);
                if (player == null) {
                    return ErrorCode.YOU_HAVE_NOT_LOGIN;
                }
                return gameUserService.reconnection(player);
            }
            case "getUserRecodeByUserId": {

                Player player = GameManager.getPlayerByCtx(ctx);
                if (player == null) {
                    return ErrorCode.YOU_HAVE_NOT_LOGIN;
                }
                int type = params.getInt("type");
                return gameUserService.getUserRecodeByUserId(player, type);

            }
            case "bindReferrer": {
                Player player = GameManager.getPlayerByCtx(ctx);
                if (player == null) {
                    return ErrorCode.YOU_HAVE_NOT_LOGIN;
                }
                int referrerId = params.getInt("referrerId");
                return gameUserService.bindReferrer(player, referrerId);
            }

            case "getUserImage": {
//                return gameUserService.getUserImage(userId,ctx);
            }
            case "register": {
//                return gameUserService.register(userId,ctx);
            }
            case "giveOtherMoney": {
                Player player = GameManager.getPlayerByCtx(ctx);
                Long accepterId = Long.parseLong(params.getString("accepterId"));
                int money = Integer.parseInt(params.getString("money"));
                return gameUserService.giveOtherMoney(player, accepterId, money);
            }
            case "getNickNamePlayer":{
                Player giver = GameManager.getPlayerByCtx(ctx);
                Long accepterUserId = Long.parseLong(params.getString("accepterId"));
                return gameUserService.getNickNamePlayer(giver, accepterUserId);
            }
            case "getUserCreateRoomList": {
                Player player = GameManager.getPlayerByCtx(ctx);
                return gameUserService.getUserCreateRoomList(player);
            }

            case "deleteRoomByRoomId": {
                Player player = GameManager.getPlayerByCtx(ctx);
                String roomId = params.getString("roomId");
                return gameUserService.deleteRoomByRoomId(player,roomId);
            }

            default:

                return ErrorCode.REQUEST_PARAM_ERROR;
        }
    }

    private int dispatchRoomService(String method, JSONObject params, ChannelHandlerContext ctx) {
        Player player = GameManager.getPlayerByCtx(ctx);
        if (player == null) {
            return ErrorCode.YOU_HAVE_NOT_LOGIN;
        }

        switch (method) {
            case "createRoom":{

                int gameNumber = params.getInt("gameNumber");
                int multiple = params.getInt("maxMultiple");
                String gameType = params.optString("gameType", "0");
                return RoomDouDiZhu.createRoom(player, gameNumber, multiple,gameType);
            }
            case "createRoomDice":{

                int cricle = params.getInt("cricle");
                int personNumber = params.getInt("personNumber");
                int isSelf = params.getInt("isSelf");

                return RoomDice.createRoom(player, cricle,personNumber,isSelf);
            }
            case "searchRoomDice":{
                Long createId = params.getLong("createId");
                return RoomDice.searchRoomDice(player,createId);
            }

            case "createRoomTDK":{

                int gameNumber = params.getInt("gameNumber");
                double multiple = params.getDouble("maxMultiple");
                int personNumber = params.getInt("personNumber");
                int hasNine = params.getInt("hasNine");
                return RoomTanDaKeng.createRoom(player, gameNumber,multiple,personNumber,hasNine);
            }
            case "joinRoom": {
                String roomId = params.getString("roomId");
                Room room = GameManager.getInstance().rooms.get(roomId);
                if (room == null) {
                    return ErrorCode.CANNOT_JOIN_ROOM_NOT_EXIST;
                }
                return room.joinRoom(player);
            }
            case "joinRoomQuick":{
                double type = params.getDouble("type");
                return GoldRoomPool.getInstance().addRoom(player, type);
            }
            case "kickPerson":{//踢人
                String roomId = params.getString("roomId");
                long kickPlayerId = params.getLong("kickPlayerId");
                Room room = GameManager.getInstance().rooms.get(roomId);
                if(kickPlayerId==room.createUser){
                    return ErrorCodeDice.CANNOT_KICK_CREATER;
                }
                if(GameManager.getInstance().blackList.keySet().contains(roomId)){
                    List<Long> list = GameManager.getInstance().blackList.get(roomId);
                    if(!list.contains(kickPlayerId)){
                        list.add(kickPlayerId);
                    }
                    GameManager.getInstance().blackList.put(roomId,list);
                }else {
                    List<Long> list = new ArrayList<>();
                    list.add(kickPlayerId);
                    GameManager.getInstance().blackList.put(roomId,list);
                }
                return room.kickPlayer(player,kickPlayerId);
            }
            case "quitRoom": {
                Room room = getRoomByPlayer(player);
                if (room == null) {
                    return ErrorCode.CAN_NOT_NO_ROOM;
                }
                return room.quitRoom(player);
            }
            case "getReady": {
                Room room = getRoomByPlayer(player);
                if (room == null) {
                    return ErrorCode.CAN_NOT_NO_ROOM;
                }
                return room.getReady(player);
            }
            case "dissolveRoom": {
                Room room = getRoomByPlayer(player);
                if (room == null) {
                    return ErrorCode.CAN_NOT_NO_ROOM;
                }
                return room.dissolution(player, true, method);
            }
            case "answerIfDissolveRoom":
                Room room = getRoomByPlayer(player);
                if (room == null) {
                    return ErrorCode.CAN_NOT_NO_ROOM;
                }
                boolean isAgree = "2".equals(params.getString("answer"));
                return room.dissolution(player, isAgree, method);
            default:
                return ErrorCode.REQUEST_PARAM_ERROR;
        }
    }


    private int dispatchGameService(String method, JSONObject params, Player player) {


        Room room = getRoomByPlayer(player);
        if (room == null) {
            return ErrorCode.CAN_NOT_NO_ROOM;
        }
        Game game = room.getGame();
        if (game == null) {
            if(room instanceof RoomDice){
                return dispatchRoomService(method,params,player.getCtx());
            }
        }



        if(game instanceof GameDouDiZhu){
            return dispatchGameDDZService(method,(GameDouDiZhu) game,params,player);
        }else if(game instanceof GameTianDaKeng){
           return dispatchGameTDKService(method,(GameTianDaKeng) game,params,player);
        }else if(game instanceof GameDice){
            return dispatchGameDiceService(method,(GameDice) game,params,player);
        }
        return -1;
    }

    private int dispatchGameDDZService(String method, GameDouDiZhu game, JSONObject params, Player player){
        switch (method) {
            case "jiaoDizhu":
                boolean isJiao = params.getBoolean("isJiao");
                int score = params.optInt("score", 0);
                return game.jiaoDizhu(player, isJiao,score);
            case "qiangDizhu":
                System.out.println("isQaing = "+params);
                boolean isQiang = params.getBoolean("isQiang");
                return game.qiangDizhu(player, isQiang);
            case "play":
                System.out.println("param = "+params.toString());
                System.out.println("json = "+params.getString("cards"));
                CardStruct cardStruct = gson.fromJson(params.getString("cards"), CardStruct.class);
                return game.play(player, cardStruct);
            case "pass":
                return game.pass(player);
            default:

                return ErrorCode.REQUEST_PARAM_ERROR;
        }
    }

    private int dispatchGameTDKService(String method,GameTianDaKeng game,JSONObject params,Player player) {

        switch (method) {
            case "bet"://下注
                int betChip = params.getInt("chip");
                return game.bet(player, betChip);
            case "call"://跟注
                return game.call(player);
            case "raise"://加注，踢
                int raiseChip = params.getInt("chip");
                return game.raise(player,raiseChip);
            case "pass"://不跟
                return game.pass(player);
            case "fold"://弃牌
                return game.fold(player);
            default:
                return ErrorCode.REQUEST_PARAM_ERROR;
        }
    }

    private int dispatchGameDiceService(String method, GameDice game, JSONObject params, Player player) {

        switch (method) {
            case "bet"://下注
                int betChip = params.getInt("chip");
                int betChip2 = params.getInt("chip2");
                int betChip3 = params.getInt("chip3");
                return game.bet(player, betChip,betChip2,betChip3);
            case "rock"://摇骰子
                return game.rock(player);
            case "kill"://杀
                Long userId = params.getLong("userId");
                return game.kill(player,userId);
            case "killAll"://杀
                return game.killAll(player);
            default:
                return ErrorCode.REQUEST_PARAM_ERROR;
        }
    }


    private int dispatchChatService(String method, JSONObject params, ChannelHandlerContext ctx) {
        Player player = GameManager.getPlayerByCtx(ctx);
        if (player == null) {
            return -1;
        }
        GameChatService chatService = SpringUtil.getBean(GameChatService.class);
        switch (method) {
            case "sendMessageToOne": {
                long acceptUserId = params.getLong("acceptUserId");
                String messageType = params.getString("messageType");
                String message = params.getString("message");
                return chatService.sendMessageToOne(player, acceptUserId, messageType, message);
            }
            case "sendMessage": {
                String messageType = params.getString("messageType");
                String message = params.getString("message");
                return chatService.sendMessage(player, messageType, message);
            }
            default: {
                return ErrorCode.REQUEST_PARAM_ERROR;
            }


        }

    }

    private Room getRoomByPlayer(Player player) {
        String roomId = GameManager.getInstance().getUserRoom().get(player.getUserId());
        if (roomId == null) {
            return null;
        }
        return GameManager.getInstance().rooms.get(roomId);
    }

}
