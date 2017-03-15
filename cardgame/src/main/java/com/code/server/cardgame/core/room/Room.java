package com.code.server.cardgame.core.room;

import com.code.server.db.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sunxianping on 2017/3/14.
 */
public class Room {

    protected List<Integer> users = new ArrayList<>();//用户列表
    protected List<User> userList = new ArrayList<>();//用户列表

    public List<Integer> getUsers() {
        return users;
    }

    public void setUsers(List<Integer> users) {
        this.users = users;
    }

    public List<User> getUserList() {
        return userList;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
    }
}
