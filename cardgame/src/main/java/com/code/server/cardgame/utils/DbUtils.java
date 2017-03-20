package com.code.server.cardgame.utils;

import com.code.server.cardgame.config.ServerConfig;
import com.code.server.cardgame.core.GameManager;
import com.code.server.cardgame.core.Player;
import com.code.server.db.Service.UserService;
import com.code.server.db.model.User;

import java.util.ArrayList;

/**
 * Created by sun on 2015/8/25.
 */
public final class DbUtils {

    public static UserService userService = SpringUtil.getBean(UserService.class);
    public static ServerConfig serverConfig = SpringUtil.getBean(ServerConfig.class);

    public static void saveUsers() {

        int temp = 0;
        ArrayList<Player> players = new ArrayList<>();
        players = (ArrayList) GameManager.getInstance().getPlayers().values();

        for (Player p : players) {
            userService.userDao.save(p.getUser());
            temp++;
            if (temp%1000==0){//1000条数据休眠一段时间
                try {
                    Thread.sleep(serverConfig.getDbSaveTime()/60);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static void saveUser(Player player) {
        userService.userDao.save(player.getUser());
    }

}
