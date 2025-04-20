package com.mj.remotecontroller;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ConnectContainer {
    @JsonProperty("id")
    private Integer id;

    @JsonProperty("controlledChannelId")
    private String controlledChannelId;

    @JsonProperty("controlChannelId")
    private String controlChannelId;
}
