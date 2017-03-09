package com.code.server.db.Service;


import com.code.server.db.dao.IUserDao;
import com.code.server.db.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by win7 on 2017/3/8.
 */

@Service("userService")
public class UserService {

    @Autowired
    private IUserDao userDao;


    public void test(){
        List<User> users = userDao.findByName("1");
        System.out.println(users.size());
    }
}
