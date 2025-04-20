package com.mj.lrp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mj.lrp.util.Utils;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Pay {
    @JsonProperty("payNo")
    private String payNo;

    @JsonProperty("userId")
    private int userId;

    @JsonProperty("vip")
    private long vip;

    @JsonProperty("createTime")
    private long createTime;

    @JsonProperty("type")
    private int type;

    @JsonProperty("description")
    private String description;

    public Pay(String payNo, int payType, int userId) {
        this.createTime = System.currentTimeMillis();
        this.payNo = payNo;
        this.userId = userId;
        this.type = payType;
        this.description = Utils.getPriceByPayType(type)+" "+Utils.getDescribeByPayType(type);
        this.vip = Utils.getVipByPayType(type);
    }
}
