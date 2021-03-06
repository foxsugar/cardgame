package com.code.server.cardgame.utils;

import com.code.server.cardgame.config.ServerConfig;
import com.code.server.cardgame.core.GameManager;
import com.code.server.cardgame.core.Player;
import com.code.server.db.Service.ConstantService;
import com.code.server.db.Service.UserService;
import com.code.server.db.model.Constant;
import com.code.server.db.model.ServerInfo;
import com.code.server.db.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.GenericArrayType;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

/**
 * 项目名称：${project_name}
 * 类名称：${type_name}
 * 类描述：
 * 创建人：Clark
 * 创建时间：${date} ${time}
 * 修改人：Clark
 * 修改时间：${date} ${time}
 * 修改备注：
 *
 * @version 1.0
 */
public class SaveUserTimerTask extends TimerTask{
    private final Logger logger = LoggerFactory.getLogger(SaveUserTimerTask.class);
    private ServerConfig serverConfig = SpringUtil.getBean(ServerConfig.class);
    private UserService userService = SpringUtil.getBean(UserService.class);


    @Override
    public void run() {

        try {
            //保存玩家
            List<User> users = new ArrayList<>();
            users.addAll(GameManager.getInstance().getSaveUser2DB());
            logger.warn("定时保存玩家个数 : "+ users.size());
            userService.batchUpdate(users);
            GameManager.getInstance().getSaveUser2DB().removeAll(users);


            //从内存中删除玩家
            long now = System.currentTimeMillis();

            List<Player> removePlayers = new ArrayList<>();
            logger.warn("要删除玩家的个数 : "+ GameManager.getInstance().getKickUser().size());
            for (Player player : GameManager.getInstance().getKickUser()) {
                //在房间中的玩家 不清理
                if(GameManager.getInstance().getUserRoom().containsKey(player.getUserId())){
                    continue;
                }
                if (now - player.getLastSendMsgTime() >= serverConfig.getKickTime()) {
                    userService.save(player.getUser());
                    GameManager.getInstance().removePlayer(player);
                    removePlayers.add(player);
                }
            }
            logger.warn("从内存中删除玩家个数 : "+ removePlayers.size());
            GameManager.getInstance().getKickUser().removeAll(removePlayers);
        } catch (Exception e) {
            logger.error("定时保存数据错误",e);
        }



    }
}
