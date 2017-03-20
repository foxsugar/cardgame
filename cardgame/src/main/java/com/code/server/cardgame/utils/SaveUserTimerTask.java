package com.code.server.cardgame.utils;

import com.code.server.cardgame.core.GameManager;
import com.code.server.db.Service.UserService;
import com.code.server.db.model.User;

import java.util.ArrayList;
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

    UserService userService = SpringUtil.getBean(UserService.class);
    @Override
    public void run() {
        DbUtils.saveUsers();
    }
}
