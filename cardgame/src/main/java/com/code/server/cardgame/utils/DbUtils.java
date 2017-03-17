package com.code.server.cardgame.utils;

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

    public static void saveUsers() {
        ArrayList<User> users = new ArrayList<>();
        users = (ArrayList) GameManager.getInstance().getPlayers().values();

        userService.userDao.save(users);
    }

    public static void saveUser(Player player) {
        userService.userDao.save(player.getUser());
    }

}
