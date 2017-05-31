package com.code.server.db.dao;

import com.code.server.db.model.Charge;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by Administrator on 2017/5/23.
 */
public interface ChargeDao extends PagingAndSortingRepository<Charge, Long> {

    Charge getChargeByOrderid(String orderid);

}
