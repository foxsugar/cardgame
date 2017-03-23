package com.code.server.db.Service;


import com.code.server.db.dao.IUserDao;
import com.code.server.db.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

/**
 * Created by win7 on 2017/3/8.
 */

@Service("userService")
public class UserService {

    @PersistenceContext
    public EntityManager em;

    @Autowired
    public IUserDao userDao;


    public void test(){

    }


    public User getUserByAccountAndPassword(String account, String password) {
        return userDao.getUserByAccountAndPassword(account, password);
    }

    public User save(User user) {

        return userDao.save(user);


    }

    @Transactional
    public void batchUpdate(List list) {
        for (int i = 0; i < list.size(); i++) {
            em.merge(list.get(i));
            if (i % 30 == 0) {
                em.flush();
                em.clear();
            }
        }
    }
}
