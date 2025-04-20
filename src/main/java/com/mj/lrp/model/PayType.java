package com.mj.lrp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class PayType {
    @JsonProperty("payType")
    private int payType;
    @JsonProperty("price")
    private String price;
    @JsonProperty("humanizedVip")
    private String humanizedVip;
    @JsonProperty("vip")
    private long vip;
    @JsonProperty("image")
    private String image;
    @JsonProperty("description")
    private String description;

    public PayType(int payType, String price, String humanizedVip, long vip, String image, String description) {
        this.payType = payType;
        this.price = price;
        this.humanizedVip = humanizedVip;
        this.vip = vip;
        this.image = image;
        this.description = description;
    }
}
