package com.mj.lrp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mj.lrp.controller.UserController;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@Data
public class PayLog {
    @JsonProperty("payNo")
    private String payNo;

    @JsonProperty("userId")
    private Integer userId;

    @JsonProperty("username")
    private String username;

    @JsonProperty("payTime")
    private Date payTime;

    @JsonProperty("type")
    private Integer type;

    @JsonProperty("description")
    private String description;

    public PayLog(Pay pay) {
        if(pay!=null) {
            this.payNo = pay.getPayNo();
            this.userId = pay.getUserId();
            this.username = UserController.userController.userService.getUserByUserId(userId)!=null?
                    UserController.userController.userService.getUserByUserId(userId).getUsername():null;
            this.payTime = new Date(pay.getCreateTime());
            this.type = pay.getType();
            this.description = pay.getDescription();
        }
    }
}
