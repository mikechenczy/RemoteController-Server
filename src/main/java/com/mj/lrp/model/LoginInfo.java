package com.mj.lrp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@Data
public class LoginInfo {

    @JsonProperty("userId")
    private int userId;

    @JsonProperty("username")
    private String username;

    @JsonProperty("lastTime")
    private Date lastTime;

    @JsonProperty("location")
    private String location;

    @JsonProperty("device")
    private String device;

    @JsonProperty("version")
    private String version;
}
