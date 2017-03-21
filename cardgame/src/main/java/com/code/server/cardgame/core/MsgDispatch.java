package com.code.server.cardgame.core;

import com.code.server.cardgame.Message.MessageHolder;
import com.code.server.cardgame.core.game.GameDouDiZhu;
import com.code.server.cardgame.core.room.Room;
import com.code.server.cardgame.core.room.RoomDouDiZhu;
import com.code.server.cardgame.response.ErrorCode;
import com.code.server.cardgame.response.ResponseVo;
import com.code.server.cardgame.service.GameUserService;
import com.code.server.cardgame.utils.SpringUtil;
import com.google.gson.Gson;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Service;

/**
 * Created by sun on 2015/8/21.
 */

@Service
public class MsgDispatch {


    private Gson gson = new Gson();

    public static void sendMsg(ChannelHandlerContext ctx, Object msg) {
        ctx.writeAndFlush(msg);
    }


    public void handleMessage(MessageHolder msgHolder) {
        Object message = msgHolder.message;
        JSONObject jSONObject = (JSONObject) message;
        System.out.println("处理消息== " + jSONObject);
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

    }

    private int dispatchAllMsg(String service, String method, JSONObject params, ChannelHandlerContext ctx) {
        switch (service) {
            case "userService":
                return dispatchUserService(method, params, ctx);
            case "roomService":
                return dispatchRoomService(method, params, ctx);
            case "gameService":
                return dispatchGameService(method, params, ctx);
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
            case "getUserMessage":
                return gameUserService.getUserMessage(GameManager.getPlayerByCtx(ctx));
            case "reconnection":
                return gameUserService.reconnection(GameManager.getPlayerByCtx(ctx));

            case "getUserImage":
//                return gameUserService.getUserImage(userId,ctx);

            case "register":
//                return gameUserService.register(userId,ctx);


            default:

                return ErrorCode.REQUEST_PARAM_ERROR;
        }
    }

    private int dispatchRoomService(String method, JSONObject params, ChannelHandlerContext ctx) {
        Player player = GameManager.getPlayerByCtx(ctx);
        if (player == null) {
            return -1;
        }

        switch (method) {
            case "createRoom":
                int gameNumber = params.getInt("gameNumber");
                int multiple = params.getInt("maxMultiple");
                return RoomDouDiZhu.createRoom(player, gameNumber, multiple);
            case "joinRoom": {
                String roomId = params.getString("roomId");
                RoomDouDiZhu room = GameManager.getInstance().rooms.get(roomId);
                if (room == null) {
                    return ErrorCode.CANNOT_JOIN_ROOM_NOT_EXIST;
                }
                return room.joinRoom(player);
            }
            case "quitRoom": {
                RoomDouDiZhu room = getRoomByPlayer(player);
                if (room == null) {
                    return ErrorCode.CAN_NOT_NO_ROOM;
                }
                return room.quitRoom(player);
            }
            case "getReady": {
                RoomDouDiZhu room = getRoomByPlayer(player);
                if (room == null) {
                    return ErrorCode.CAN_NOT_NO_ROOM;
                }
                return room.getReady(player);
            }
            case "dissolveRoom": {
                RoomDouDiZhu room = getRoomByPlayer(player);
                if (room == null) {
                    return ErrorCode.CAN_NOT_NO_ROOM;
                }
                boolean isAgree = params.getBoolean("agreeOrNot");
                return room.dissolution(player, isAgree);
            }
            default:
                return ErrorCode.REQUEST_PARAM_ERROR;
        }
    }


    private int dispatchGameService(String method, JSONObject params, ChannelHandlerContext ctx) {
        Player player = GameManager.getPlayerByCtx(ctx);
        if (player == null) {
            return -1;
        }

        RoomDouDiZhu room = getRoomByPlayer(player);
        if (room == null) {
            return ErrorCode.CAN_NOT_NO_ROOM;
        }
        GameDouDiZhu game = room.getGame();
        if (game == null) {
            return ErrorCode.CAN_NOT_NO_GAME;
        }

        switch (method) {
            case "jiaoDizhu":
                boolean isJiao = params.getBoolean("isJiao");
                return game.jiaoDizhu(player, isJiao);
            case "qiangDizhu":
                boolean isQiang = params.getBoolean("isQiang");
                return game.qiangDizhu(player, isQiang);
            case "play":
                CardStruct cardStruct = gson.fromJson(params.getString("cards"), CardStruct.class);
                return game.play(player, cardStruct);

            default:

                return ErrorCode.REQUEST_PARAM_ERROR;
        }
    }


    private RoomDouDiZhu getRoomByPlayer(Player player) {
        String roomId = GameManager.getInstance().getUserRoom().get(player.getUserId());
        if (roomId == null) {
            return null;
        }
        return GameManager.getInstance().rooms.get(roomId);
    }


}
