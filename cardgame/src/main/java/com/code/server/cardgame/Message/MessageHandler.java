package com.code.server.cardgame.Message;

import com.code.server.cardgame.core.GameManager;
import com.code.server.cardgame.core.Player;
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
public class MessageHandler {



    @Autowired
    private UserService userServicedao;

    @Autowired
    private IUserDao userDao;

    public static AttributeKey<Long> attributeKey = AttributeKey.newInstance("userId");
    private Gson gson = new Gson();
    private IdWorker idWorker = new IdWorker(1, 1);

    public void handleMessage(MessageHolder msgHolder) {
        System.out.println("000000000000000000000");
        Object message = msgHolder.message;
        JSONObject jSONObject = (JSONObject) message;
        String service = jSONObject.getString("service");
        String method = jSONObject.getString("method");
        JSONObject params = jSONObject.getJSONObject("params");

        //逻辑
        int code = dispatchAllMsg(service, method, params, msgHolder.ctx);
        //客户端要的方法返回
        ResponseVo vo = new ResponseVo(service, method, code);
        msgHolder.ctx.write(gson.toJson(vo));

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
//        UserService userService1 = SpringUtil.getBean(UserService.class);
//        System.out.println(userService1);
        GameUserService gameUserService = SpringUtil.getBean(GameUserService.class);
        switch (method) {
            case "login":
            case "appleCheck":

//                UserService userService1 = SpringUtil.getBean(UserService.class);
//                UserService userService = (UserService)SpringUtil.getBean("userService");
//                System.out.println(userService == userService1);
                return gameUserService.appleCheck(ctx);

            default:

                return -1;
        }
    }

    private int dispatchRoomService(String method, JSONObject params, ChannelHandlerContext ctx) {
        if (ctx.channel().attr(attributeKey).get() != null) {
            long uid = ctx.channel().attr(attributeKey).get();
            Player player = GameManager.getInstance().players.get(uid);
        }

        switch (method) {
            case "create":
            default:

                return -1;
        }
    }


}
