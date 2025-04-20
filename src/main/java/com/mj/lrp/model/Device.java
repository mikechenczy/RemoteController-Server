package com.mj.lrp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class Device {
    @JsonProperty("deviceId")
    public String deviceId;
    @JsonProperty("userId")
    public int userId;
    @JsonProperty("username")
    public String username;
    @JsonProperty("connectId")
    public String connectId;
    @JsonProperty("connectPin")
    public String connectPin;
    @JsonProperty("controlId")
    public int controlId;
    @JsonProperty("lastLogin")
    public Date lastLogin;
    @JsonProperty("version")
    public String version;
    @JsonProperty("ip")
    public String ip;
    @JsonProperty("deviceInfo")
    public String deviceInfo;
}
