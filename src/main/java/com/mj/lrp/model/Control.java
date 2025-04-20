package com.mj.lrp.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mj.lrp.controller.UserController;
import com.mj.lrp.control.ControlService;
import com.mj.lrp.server.Handler;
import com.mj.lrp.service.UserVipService;
import com.mj.lrp.util.Utils;
import jakarta.websocket.Session;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Data
@NoArgsConstructor
public class Control {
    private int controlId;
    private int connectId;
    private String controllerDeviceId;
    private String controlledDeviceId;

    public void removeControl(String deviceId) throws IOException {
        if(controlledDeviceId.equals(deviceId)) {
            ControlService.removeControl(controlId);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", "controlRemoved");
            jsonObject.put("controlled", false);
            jsonObject.put("controlId", controlId);
            Device device = UserController.userController.deviceService.getDeviceByDeviceId(deviceId);
            if(device !=null) {
                device.setControlId(0);
                UserController.userController.deviceService.insertOrUpdate(device);
            }
            device = UserController.userController.deviceService.getDeviceByDeviceId(controllerDeviceId);
            if(device !=null) {
                device.setControlId(0);
                UserController.userController.deviceService.insertOrUpdate(device);
            }
            Session chx = Handler.getHandler(controllerDeviceId);
            Utils.sendMessageSync(chx, jsonObject.toString());
            return;
        }
        ControlService.removeControl(controlId);
        Device device = UserController.userController.deviceService.getDeviceByDeviceId(controlledDeviceId);
        if(device !=null) {
            device.setControlId(0);
            UserController.userController.deviceService.insertOrUpdate(device);
        }
        device = UserController.userController.deviceService.getDeviceByDeviceId(deviceId);
        if(device !=null) {
            device.setControlId(0);
            UserController.userController.deviceService.insertOrUpdate(device);
        }
        notifyControlRemoved();
    }

    public void notifyControlEstablished() {
        Session chx = Handler.getHandler(controlledDeviceId);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "control");
        jsonObject.put("controlled", true);
        jsonObject.put("controlId", controlId);
        Utils.sendMessage(chx, jsonObject.toString(), null);
        jsonObject.replace("controlled", false);
        JSONObject info = JSON.parseObject(UserController.userController.deviceService.getDeviceByDeviceId(controlledDeviceId).getDeviceInfo());
        jsonObject.put("width", info.getInteger("width"));
        jsonObject.put("height", info.getInteger("height"));
        chx = Handler.getHandler(controllerDeviceId);
        Utils.sendMessage(chx, jsonObject.toString(), null);
    }

    public void notifyControlRemoved() {
        Session chx = Handler.getHandler(controlledDeviceId);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "controlRemoved");
        jsonObject.put("controlled", true);
        Utils.sendMessage(chx, jsonObject.toString(), null);
        jsonObject.replace("controlled", false);
        chx = Handler.getHandler(controllerDeviceId);
        Utils.sendMessage(chx, jsonObject.toString(), null);
    }

    public void sendData(boolean controlled, JSONObject data) throws IOException {
        if(!controlled) {
            Utils.sendBytesSync(Handler.getHandler(controlledDeviceId), ByteBuffer.wrap(data.toString().getBytes(StandardCharsets.UTF_8)));
            return;
        }
        Session chx = Handler.getHandler(controllerDeviceId);
        Device device = UserController.userController.deviceService.getDeviceByDeviceId(controllerDeviceId);
        if(device==null)
            return;
        User user = UserController.userController.userService.getUserByUserId(device.getUserId());
        if(user==null)
            return;
        user.setVip(UserVipService.get(user.getUserId())-data.toString().getBytes().length);
        UserVipService.insertOrUpdate(user.getUserId(), user.getVip());
        if (UserVipService.get(user.getUserId())<=0) {
            JSONObject response = new JSONObject();
            response.put("type", "bandwidthRunOut");
            response.put("reason", "流量已使用完");
            Utils.sendMessageSync(chx, response.toString());
            removeControl(controllerDeviceId);
            return;
        }
        Utils.sendBytesSync(chx, ByteBuffer.wrap(data.toString().getBytes(StandardCharsets.UTF_8)));
    }
}
