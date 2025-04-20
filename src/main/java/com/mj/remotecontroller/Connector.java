package com.mj.remotecontroller;

import java.sql.*;

public class Connector {
    public static Connection connection;
    public static void connect() {
        String driver ="com.mysql.cj.jdbc.Driver";
        String url ="jdbc:mysql://localhost:3306/remotecontroller?useSSL=false&useUnicode=true&characterEncoding=utf8&serverTimezone=UTC";
        String user ="root";
        String password ="czy078579";
        try {
            Class.forName(driver);//加载驱动程序
            connection = DriverManager.getConnection(url,user,password);//连接数据库
            /*if(!connection.isClosed())
                System.out.println("Succeeded connecting to the Database!");
            Statement statement =connection.createStatement();//操作数据库
            String sql ="select * from student";//要执行的sql语句
            ResultSet rs=statement.executeQuery(sql);
            System.out.println("-----------------------------------");
            System.out.println("学号"+"\t"+"姓名"+"\t"+"年龄"+"\t"+"地址");
            System.out.println("-----------------------------------");
            while(rs.next()){
                String name = rs.getString("sname");
                String age = rs.getString("age");
                String address = rs.getString("address");
                System.out.println(rs.getString("sno")+"\t"+name+"\t"+age+"\t"+address);
            }
            rs.close();
            connection.close();*/
        } catch(ClassNotFoundException e) {
            System.out.println("Sorry,can not find the Driver!");
            e.printStackTrace();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }
}
