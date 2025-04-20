package com.mj.lrp.service;

import com.mj.lrp.dao.PayLogDao;
import com.mj.lrp.model.PayLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PayLogService {

    @Autowired
    private PayLogDao payLogDao;

    public PayLog getByPayNo(String payNo) {
        return payLogDao.getByPayNo(payNo);
    }

    public List<PayLog> getAll() {
        return payLogDao.getAll();
    }

    public int insert(PayLog payLog) {
        return payLogDao.insert(payLog);
    }

    public int delete(String payNo) {
        return payLogDao.delete(payNo);
    }
}