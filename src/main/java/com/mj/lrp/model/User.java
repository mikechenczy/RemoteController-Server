package com.mj.lrp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class User {

    @JsonProperty("userId")
    private int userId;

    @JsonProperty("username")
    private String username;

    @JsonProperty("password")
    private String password;

    @JsonProperty("email")
    private String email;

    @JsonProperty("createTime")
    private Date createTime;

    @JsonProperty("vip")
    private long vip;

    @JsonProperty("vipType")
    private int vipType;

    @JsonProperty("devices")
    private String devices;

    public User() {
        setDevices("[]");
    }
}
