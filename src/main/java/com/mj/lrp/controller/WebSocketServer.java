package com.mj.lrp.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mj.lrp.control.ControlService;
import com.mj.lrp.model.Control;
import com.mj.lrp.model.Device;
import com.mj.lrp.model.User;
import com.mj.lrp.server.ControlHandler;
import com.mj.lrp.server.Handler;
import com.mj.lrp.server.Tick;
import com.mj.lrp.service.UserVipService;
import com.mj.lrp.util.Utils;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

/**
 * @author： xxt
 * @date： 2022/5/23 16:27
 * @Description： WebSocket操作类
 */
@ServerEndpoint("/websocket/{deviceId}")
@Component
@Slf4j
public class WebSocketServer {

    private Session session;
    private String deviceId;
    
    private Tick updateBandwidthTick;


    /**
     * 建立WebSocket连接
     *
     * @param deviceId 用户ID
     */
    @OnOpen
    public void onOpen(Session session, @PathParam(value = "deviceId") String deviceId) throws IOException {
        //log.info("WebSocket建立连接中,连接设备ID：{}", deviceId);
        this.session = session;
        this.deviceId = deviceId;
        if(deviceId==null) {
            session.close();
            return;
            //TODO: Send Message
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "connectIdAndPin");
        Utils.OnSendFailedListener onSendFailedListener = new Utils.OnSendFailedListener() {
            @Override
            public void onSendFailed(Session session, String message, IOException e) {
                //e.printStackTrace();
                try {
                    Handler.removeHandler(deviceId);
                    //Device device = Handler.putHandler(deviceId, session);
                    //jsonObject.put("connectId", device.getConnectId());
                    //jsonObject.put("connectPin", device.getConnectPin());
                    //Utils.sendMessage(session, jsonObject.toString(), null);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        };
        updateBandwidthTick = new Tick(2000, new Tick.OnTick() {
            @Override
            public void onTick() {
                Device device = UserController.userController.deviceService.getDeviceByDeviceId(deviceId);
                if(device==null)
                    return;
                JSONObject response = new JSONObject();
                response.put("type", "bandwidth");
                response.put("bandwidth", UserVipService.get(device.getUserId()));
                try {
                    Utils.sendMessageSync(WebSocketServer.this.session, response.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        /*longTick = new Tick(5000, new Tick.OnTick() {
            @Override
            public void onTick() {
                updateBandwidth();
            }
        });*/
        updateBandwidthTick.start();
        //longTick.start();
        Device device = Handler.putHandler(deviceId, session);
        jsonObject.put("connectId", device.getConnectId());
        jsonObject.put("connectPin", device.getConnectPin());
        Utils.sendMessage(session, jsonObject.toString(), onSendFailedListener);
        jsonObject.put("type", "speedLimit");
        jsonObject.put("speedLimited", false);
        jsonObject.put("maxSpeed", 1024*1024);
        Utils.sendMessage(session, jsonObject.toString(), onSendFailedListener);
        new Thread(() -> {
            while (session.isOpen()) {
                try {
                    //System.out.println("SEND");
                    Utils.sendMessageSync(session, "{\"type\": \"isOnline\"}");
                } catch (Exception e) {
                    //e.printStackTrace();
                    return;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }).start();
    }

    /**
     * 发生错误
     *
     * @param throwable e
     */
    @OnError
    public void onError(Throwable throwable) {
        //throwable.printStackTrace();
    }

    /**
     * 连接关闭
     */
    @OnClose
    public void onClose(Session session) throws IOException {
        updateBandwidthTick.interrupt();
        if(session.getMaxIdleTimeout()==0) {
            Handler.removeHandler(deviceId);
        }
        log.info("连接断开，设备ID：{}，在线人数：{}", deviceId, Handler.count());
    }

    /**
     * 接收客户端消息
     *
     * @param message 接收的消息
     */
    @OnMessage(maxMessageSize=2155380*10)
    public void onMessage(String message) throws IOException {
        //log.info("收到客户端发来的消息：{}", message);
        if(message.equals("heartbeat"))
            return;
        JSONObject request = JSON.parseObject(message);
        switch (request.getString("type")) {
            case "deviceRemove" -> {
                Device device = UserController.userController.deviceService.getDeviceByDeviceId(deviceId);
                if (device == null)
                    return;
                if (!request.containsKey("deviceId"))
                    return;
                User user = UserController.userController.userService.getUserByUserId(device.getUserId());
                if (user == null)
                    return;
                JSONArray jsonArray = JSON.parseArray(user.getDevices());
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (request.getString("deviceId").equals(jsonObject.getString("deviceId"))) {
                        jsonArray.remove(i);
                        break;
                    }
                }
                user.setDevices(jsonArray.toString());
                UserController.userController.userService.update(user);
                Handler.refreshDevicesForUser(user.getUserId());
            }
            case "remote" -> {
                if (request.getBooleanValue("enabled")) {
                    if (!Handler.enabledDevices.contains(deviceId)) {
                        Handler.enabledDevices.add(deviceId);
                    }
                } else {
                    if (Handler.enabledDevices.contains(deviceId)) {
                        Device device = UserController.userController.deviceService.getDeviceByDeviceId(deviceId);
                        if (device != null && device.getControlId() != 0) {
                            Control control = ControlService.get(device.getControlId());
                            if (control != null) {
                                control.removeControl(deviceId);
                                device.setControlId(0);
                                UserController.userController.deviceService.update(device);
                            }
                        }
                        Handler.enabledDevices.remove(deviceId);
                    }
                }
                Handler.refreshDevicesAt(deviceId);
            }
            case "basicData" -> {
                Device device = UserController.userController.deviceService.getDeviceByDeviceId(deviceId);
                if (device == null)
                    return;
                device.setLastLogin(new Date());
                device.setVersion(request.getString("version"));
                device.setIp(request.getString("ip"));
                device.setDeviceInfo(request.getString("deviceInfo"));
                device.setUserId(request.getInteger("userId"));
                //User user = UserController.userController.userService.getUserByUserId(device.userId);
                //if(user!=null)
                //    bandwidth = user.getVip();
                UserController.userController.deviceService.update(device);
                Handler.refreshDevicesAt(deviceId);
            }
            case "devices" -> {
                Device device = UserController.userController.deviceService.getDeviceByDeviceId(deviceId);
                if (device == null)
                    return;
                User user = UserController.userController.userService.getUserByUserId(device.getUserId());
                if (user == null)
                    return;
                if (request.containsKey("devices")) {
                    user.setDevices(request.getString("devices"));
                    UserController.userController.userService.update(user);
                }
                Handler.refreshDevicesForUser(user.getUserId());
            }
            case "send" -> {
                int controlId = request.getInteger("controlId");
                Control control = ControlService.getControl(controlId);
                if (control == null) {
                    JSONObject response = new JSONObject();
                    response.put("type", "sendFailed");
                    response.put("controlled", request.getBoolean("controlled"));
                    response.put("reason", "控制失效");
                    Utils.sendMessageSync(session, response.toString());
                    return;
                }
                control.sendData(request.getBoolean("controlled"), request.getJSONObject("data"));
            }
            case "changePin" -> {
                Device device = UserController.userController.deviceService.getDeviceByDeviceId(deviceId);
                if (device == null)
                    return;
                device.setConnectPin(UserController.userController.deviceService.generateConnectPin());
                UserController.userController.deviceService.update(device);
                JSONObject response = new JSONObject();
                response.put("type", "connectIdAndPin");
                response.put("connectId", device.getConnectId());
                response.put("connectPin", device.getConnectPin());
                Utils.sendMessageSync(this.session, response.toString());
            }
            case "connect" -> {
                if (ControlService.containsDeviceId(deviceId) != 0) {
                    JSONObject response = new JSONObject();
                    response.put("type", "controlFailed");
                    response.put("reason", "您已在控制");
                    Utils.sendMessageSync(this.session, response.toString());
                    return;
                }
                Device device = UserController.userController.deviceService.getDeviceByConnectId(request.getString("connectId"));
                if (device == null) {
                    JSONObject response = new JSONObject();
                    response.put("type", "controlFailed");
                    response.put("reason", "控制码不存在");
                    Utils.sendMessageSync(this.session, response.toString());
                    return;
                }
                if (!device.getConnectPin().equals(request.getString("connectPin"))) {
                    JSONObject response = new JSONObject();
                    response.put("type", "controlFailed");
                    response.put("reason", "控制密码错误");
                    Utils.sendMessageSync(this.session, response.toString());
                    return;
                }
                if (!Handler.handles.containsKey(device.getDeviceId())) {
                    JSONObject response = new JSONObject();
                    response.put("type", "controlFailed");
                    response.put("reason", "此设备离线");
                    Utils.sendMessageSync(this.session, response.toString());
                    return;
                }
                if (device.getDeviceId().equals(deviceId)) {
                    JSONObject response = new JSONObject();
                    response.put("type", "controlFailed");
                    response.put("reason", "不能控制自己");
                    Utils.sendMessageSync(this.session, response.toString());
                    return;
                }
                if (ControlService.containsDeviceId(device.getDeviceId()) == 1) {
                    JSONObject response = new JSONObject();
                    response.put("type", "controlFailed");
                    response.put("reason", "对方正在被控制");
                    Utils.sendMessageSync(this.session, response.toString());
                    return;
                }
                if (!Handler.enabledDevices.contains(device.getDeviceId())) {
                    JSONObject response = new JSONObject();
                    response.put("type", "controlFailed");
                    response.put("reason", "此设备未开启连接码模式的远程控制");
                    Utils.sendMessageSync(this.session, response.toString());
                    return;
                }

                //if (device.getControlId() != 0) {
                //    ControlHandler.enterControl(device.getControlId(), deviceId);
                //    JSONObject response = new JSONObject();
                //    response.put("type", "control");
                //    response.put("controlled", false);
                //    response.put("controlId", device.getControlId());
                //    JSONObject info = JSON.parseObject(device.getDeviceInfo());
                //    response.put("width", info.getInteger("width"));
                //    response.put("height", info.getInteger("height"));
                //    Utils.sendMessageSync(this.session, response.toString());
                //} else {
                ControlHandler.establishNewControl(device.getDeviceId(), deviceId);
                //}
            }
            case "exitControl" -> {
                if (ControlService.containsDeviceId(deviceId) == 0) {
                    JSONObject response = new JSONObject();
                    response.put("type", "exitFailed");
                    response.put("reason", "您不在控制");
                    Utils.sendMessageSync(session, response.toString());
                    return;
                }
                System.out.println("EXIT");
                ControlService.getControlFromDeviceId(deviceId).removeControl(deviceId);
            }
        }
    }
}

