package com.mj.lrp.service;

import com.mj.lrp.dao.LoginInfoDao;
import com.mj.lrp.model.LoginInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("loginInfoService")
public class LoginInfoService {

    @Autowired
    private LoginInfoDao loginInfoDao;

    public LoginInfo getByUserId(int userId) {
        return loginInfoDao.getByUserId(userId);
    }

    public List<LoginInfo> getAll() {
        return loginInfoDao.getAll();
    }

    public int insert(LoginInfo loginInfo) {
        return loginInfoDao.insert(loginInfo);
    }

    public int delete(int userId) {
        return loginInfoDao.delete(userId);
    }

    public int deleteAll() {
        return loginInfoDao.deleteAll();
    }

    public int update(LoginInfo loginInfo) {
        return loginInfoDao.update(loginInfo);
    }
}