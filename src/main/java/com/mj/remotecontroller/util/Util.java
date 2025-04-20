package com.mj.remotecontroller.util;

import com.alibaba.fastjson.JSONObject;
import com.mj.remotecontroller.ChannelSupervise;
import com.mj.remotecontroller.ConnectContainer;
import com.mj.remotecontroller.HandleContainer;

import java.util.Random;

public class Util {
    public static Integer getRandomConnectId() {
        synchronized (ChannelSupervise.handleContainers) {
            int connectId = new Random().nextInt(900000)+100000;
            for (HandleContainer handleContainer : ChannelSupervise.handleContainers) {
                if(handleContainer.getConnectId().equals(String.valueOf(connectId)))
                    return getRandomConnectId();
            }
            return connectId;
        }
    }

    public static String getRandomConnectPin() {
        synchronized (ChannelSupervise.handleContainers) {
            String connectPin = getRandomStr(4);
            for (HandleContainer handleContainer : ChannelSupervise.handleContainers) {
                if(handleContainer.getConnectPin().equals(connectPin))
                    return getRandomConnectPin();
            }
            return connectPin;
        }
    }

    private static String getRandomStr(int n) {
        Random random = new Random();
        String val = "";
        for (int i = 0; i < n; i++) {
            String str = random.nextInt(2) % 2 == 0 ? "num" : "char";
            if ("char".equalsIgnoreCase(str)) {
                int nextInt = random.nextInt(2) % 2 == 0 ? 65 : 97;
                val += (char) (nextInt + random.nextInt(26));
            } else if ("num".equalsIgnoreCase(str)) {
                val += String.valueOf(random.nextInt(10));
            }
        }
        return val.toUpperCase();
    }

    public static Integer getRandomConnectionId() {
        synchronized (ChannelSupervise.handleContainers) {
            int id = new Random().nextInt(900000)+100000;
            for (ConnectContainer connectContainer : ChannelSupervise.connectContainers) {
                if(connectContainer.getId()==id)
                    return getRandomConnectionId();
            }
            return id;
        }
    }

    public static String getRandomNum(int digit) {
        String result = "";
        for(int i=0;i<digit;i++) {
            result = result + new Random().nextInt(10);
        }
        return result;
    }

    public static JSONObject getDefaultJSONObject(String type) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        return jsonObject;
    }
}
