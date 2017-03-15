package com.code.server.cardgame.core;

import com.code.server.cardgame.Message.MessageHolder;
import com.code.server.cardgame.core.GameManager;
import com.code.server.cardgame.core.Player;
import com.code.server.cardgame.core.game.Game;
import com.code.server.cardgame.core.room.RoomDouDiZhu;
import com.code.server.cardgame.response.ErrorCode;
import com.code.server.cardgame.response.ResponseVo;
import com.code.server.cardgame.service.GameUserService;
import com.code.server.cardgame.utils.IdWorker;
import com.code.server.cardgame.utils.SpringUtil;
import com.code.server.db.Service.UserService;
import com.code.server.db.dao.IUserDao;
import com.google.gson.Gson;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;

/**
 * Created by sun on 2015/8/21.
 */

@Service
public class MsgDispatch {

    public static AttributeKey<Long> attributeKey = AttributeKey.newInstance("userId");

    public static void sendMsg(ChannelHandlerContext ctx,Object msg){
        ctx.writeAndFlush(msg);
    }


    public void handleMessage(MessageHolder msgHolder) {
        Object message = msgHolder.message;
        JSONObject jSONObject = (JSONObject) message;
        System.out.println("处理消息== "+jSONObject);
        String service = jSONObject.getString("service");
        String method = jSONObject.getString("method");
        JSONObject params = jSONObject.getJSONObject("params");

        //逻辑
        int code = dispatchAllMsg(service, method, params, msgHolder.ctx);
        //客户端要的方法返回
        if (code != 0) {
            ResponseVo vo = new ResponseVo(service, method, code);
            sendMsg(msgHolder.ctx,vo);
        }

    }

    private int dispatchAllMsg(String service, String method, JSONObject params, ChannelHandlerContext ctx) {
        switch (service) {
            case "userService":
                return dispatchUserService(method, params, ctx);
            case "roomService":
                return dispatchRoomService(method, params, ctx);
            default:
                return -1;
        }
    }

    private int dispatchUserService(String method, JSONObject params, ChannelHandlerContext ctx) {

        GameUserService gameUserService = SpringUtil.getBean(GameUserService.class);
        String userId = params.getString("userId");
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
                return gameUserService.checkOpenId(openId,username,image,sex,ctx);
            case "getUserMessage":
<<<<<<< HEAD
                return gameUserService.getUserMessage(userId,ctx);

            case "getUserImage":
                return gameUserService.getUserImage(userId,ctx);

            case "register":
                return gameUserService.register(userId,ctx);
=======
                return gameUserService.getUserMessage(getPlayerByCtx(ctx));
            case "reconnection":
>>>>>>> 95a1d35c610720295447625a7f963f958689a00f

            default:

                return -1;
        }
    }

    private Player getPlayerByCtx(ChannelHandlerContext ctx){
        if (ctx.channel().attr(attributeKey).get() != null) {
            long uid = ctx.channel().attr(attributeKey).get();
            return GameManager.getInstance().players.get(uid);
        }
        return null;
    }
    private int dispatchRoomService(String method, JSONObject params, ChannelHandlerContext ctx) {
        Player player = getPlayerByCtx(ctx);
        if (player == null) {
            return -1;
        }

        switch (method) {
            case "createRoom":
                int gameNumber = params.getInt("gameNumber");
                int multiple = params.getInt("maxMultiple");
                return RoomDouDiZhu.createRoom(player, gameNumber, multiple);
            case "joinRoom":
                String roomId = params.getString("roomId");

                RoomDouDiZhu room = GameManager.getInstance().rooms.get(roomId);
                if (room == null) {
                    return ErrorCode.CANNOT_JOIN_ROOM_NOT_EXIST;
                }
                return room.joinRoom(player);
            case "quitRoom":
                String roomId1 = GameManager.getInstance().getUserRoom().get(player.getUserId());
                if (roomId1 == null) {
                    return ErrorCode.CANNOT_QUIT_ROOM_NOT_IN_ROOM;
                }
                RoomDouDiZhu room1 = GameManager.getInstance().rooms.get(roomId1);
                if (room1 == null) {
                    return ErrorCode.CANNOT_QUIT_ROOM_NOT_IN_ROOM;
                }
                return room1.quitRoom(player);

            case "getReady":
                String roomId2 = GameManager.getInstance().getUserRoom().get(player.getUserId());
                if (roomId2 == null) {
                    return ErrorCode.CANNOT_QUIT_ROOM_NOT_IN_ROOM;
                }
                RoomDouDiZhu room2 = GameManager.getInstance().rooms.get(roomId2);
                if (room2 == null) {
                    return ErrorCode.CANNOT_QUIT_ROOM_NOT_IN_ROOM;
                }
                return room2.getReady(player);
            default:

                return -1;
        }
    }


}
