package com.mj.remotecontroller;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class HandleContainer {
    @JsonProperty("channelShortId")
    private String channelShortId;
    @JsonProperty("userId")
    private Integer userId;
    @JsonProperty("connectId")
    private String connectId;
    @JsonProperty("connectPin")
    private String connectPin;
}
