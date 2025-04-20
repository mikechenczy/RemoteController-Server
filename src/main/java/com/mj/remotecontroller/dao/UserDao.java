package com.mj.remotecontroller.dao;

import com.mj.remotecontroller.Connector;
import com.mj.remotecontroller.User;
import com.mj.remotecontroller.util.SqlUtil;

import java.sql.SQLException;
import java.sql.Statement;

public class UserDao {
    public static boolean insert(User user) {
        try {
            Statement statement = Connector.connection.createStatement();//操作数据库
            String sql = SqlUtil.getInsertSql("t_user", "id;"+user.getId(), "username;"+user.getUsername(), "password"+user.getPassword());
            return statement.execute(sql);
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return false;
    }
}
