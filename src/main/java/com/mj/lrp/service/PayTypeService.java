package com.mj.lrp.service;

import com.mj.lrp.dao.PayTypeDao;
import com.mj.lrp.model.PayType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PayTypeService {

    @Autowired
    private PayTypeDao payTypeDao;

    public PayType getByPayType(int payType) {
        return payTypeDao.getByPayType(payType);
    }

    public List<PayType> getAll() {
        return payTypeDao.getAll();
    }

    public int insert(PayType paytype) {
        return payTypeDao.insert(paytype);
    }

    public int delete(String payNo) {
        return payTypeDao.delete(payNo);
    }
}