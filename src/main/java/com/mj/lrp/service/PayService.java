package com.mj.lrp.service;

import com.mj.lrp.dao.PayDao;
import com.mj.lrp.model.Pay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PayService {

    @Autowired
    private PayDao payDao;

    public Pay getByPayNo(String payNo) {
        return payDao.getByPayNo(payNo);
    }

    public List<Pay> getPayByUserId(int userId) {
        return payDao.getPayByUserId(userId);
    }

    public List<Pay> getAll() {
        return payDao.getAll();
    }

    public int insert(Pay payment) {
        return payDao.insert(payment);
    }

    public int delete(String payNo) {
        return payDao.delete(payNo);
    }

    public int deleteAllByUserId(int userId) {
        return payDao.deleteAllByUserId(userId);
    }
}