package com.code.server.db.Service;

import com.code.server.db.dao.ChargeDao;
import com.code.server.db.dao.IUserDao;
import com.code.server.db.model.Charge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Administrator on 2017/5/23.
 */
@Service("ChargeService")
public class ChargeService {

    @Autowired
    private ChargeDao chargeDao;

    public Charge save(Charge charge) {
        return chargeDao.save(charge);
    }


    public Charge getChargeByOrderid(String orderid) {
        return chargeDao.getChargeByOrderid(orderid);
    }

}
