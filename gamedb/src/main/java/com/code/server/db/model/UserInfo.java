package com.code.server.db.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sunxianping on 2017/4/1.
 */
public class UserInfo {
    List list = new ArrayList<>();

    public List getList() {
        return list;
    }

    public UserInfo setList(List list) {
        this.list = list;
        return this;
    }

    public UserInfo(){}
}
