package com.code.server.cardgame.bootstarp;

import com.code.server.cardgame.config.ServerState;
import com.code.server.cardgame.core.GameManager;
import com.code.server.cardgame.core.Player;
import com.code.server.cardgame.handler.GameProcessor;
import com.code.server.cardgame.utils.SpringUtil;
import com.code.server.db.Service.UserService;
import com.code.server.db.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sunxianping on 2017/3/28.
 */
public class Shutdown {

    public static void shutdown(){
        //停止接收协议
        ServerState.isWork = false;

        //处理消息
        GameProcessor.getInstance().handle();

        //保存玩家
        List<User> users = new ArrayList<>();
        for (Player player : GameManager.getInstance().players.values()) {
            users.add(player.getUser());
        }
        UserService userService = SpringUtil.getBean(UserService.class);
        userService.batchUpdate(users);
        userService.em.flush();
    }
}
