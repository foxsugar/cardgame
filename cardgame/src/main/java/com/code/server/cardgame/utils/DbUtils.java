package com.code.server.cardgame.utils;

import com.code.server.cardgame.config.ServerConfig;
import com.code.server.cardgame.core.GameManager;
import com.code.server.cardgame.core.Player;
import com.code.server.db.Service.UserService;
import com.code.server.db.model.User;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by sun on 2015/8/25.
 */
public final class DbUtils {

    public static UserService userService = SpringUtil.getBean(UserService.class);
    public static ServerConfig serverConfig = SpringUtil.getBean(ServerConfig.class);

    public static void saveUsers() {

        int temp = 0;
        Collection<User> users = new ArrayList<>();
        users = GameManager.getInstance().getUsers().values();

        for (User u : users) {
            userService.userDao.save(u);
            GameManager.getInstance().getUsers().remove(u.getId());
            temp++;
            if (temp%1000==0){//每1000次保存休眠
                try {
                    Thread.sleep(serverConfig.getDbSaveTime()/60);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static void saveUser(User user) {
        userService.userDao.save(user);
        GameManager.getInstance().getUsers().remove(user.getId());
    }

}
