package com.mj.lrp.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mj.lrp.controller.UserController;
import com.mj.lrp.control.ControlService;
import com.mj.lrp.model.Device;
import com.mj.lrp.model.Control;

import com.mj.lrp.model.User;
import com.mj.lrp.util.Utils;
import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class Handler {
    public static final ConcurrentHashMap<String, Session> handles = new ConcurrentHashMap<>();
    public static final List<String> enabledDevices = new ArrayList<>();

    public static Device putHandler(String deviceId, Session session) {
        if(handles.containsKey(deviceId)) {
            try {
                removeHandler(deviceId);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        handles.put(deviceId, session);
        log.info("建立连接，设备ID：{}，在线人数：{}", deviceId, count());
        Device device = UserController.userController.deviceService.getDeviceByDeviceId(deviceId);
        if(device!=null) {
            return device;
        }
        device = new Device();
        device.setDeviceId(deviceId);
        device.setUserId(0);
        device.setConnectId(UserController.userController.deviceService.generateConnectId());
        device.setConnectPin(UserController.userController.deviceService.generateConnectPin());
        device.setControlId(0);
        UserController.userController.deviceService.insert(device);
        return device;
    }

    public static Session getHandler(String deviceId) {
        return handles.get(deviceId);
    }

    public static boolean containsConnectId(String connectId) {
        synchronized (handles) {
            for(String deviceId : handles.keySet()) {
                Device device = UserController.userController.deviceService.getDeviceByDeviceId(deviceId);
                if(device.getConnectId().equals(connectId))
                    return true;
            }
            return false;
        }
    }

    public static boolean removeHandler(String deviceId) throws IOException {
        if (handles.containsKey(deviceId)) {
            Session session = handles.get(deviceId);
            if (session!=null && session.isOpen()) {
                session.setMaxIdleTimeout(10000);
                session.close();
            }
            handles.remove(deviceId);
            enabledDevices.remove(deviceId);
            Device device = UserController.userController.deviceService.getDeviceByDeviceId(deviceId);
            if (device != null && device.getControlId() != 0) {
                Control control = ControlService.get(device.getControlId());
                if (control != null) {
                    control.removeControl(deviceId);
                    device.setControlId(0);
                    UserController.userController.deviceService.update(device);
                }
            }
            new Thread(() -> Handler.refreshDevicesAt(deviceId)).start();
            return true;
        }
        return false;
    }

    public static int count() {
        return handles.size();
    }

    public static void refreshAllDevices() {
        synchronized (handles) {
            for (String deviceId : handles.keySet()) {
                new Thread(() -> refreshDevicesFor(deviceId)).start();
            }
        }
    }

    public static void refreshDevicesAt(String id) {
        synchronized (handles) {
            A:
            for (String deviceId : handles.keySet()) {
                Device device = UserController.userController.deviceService.getDeviceByDeviceId(deviceId);
                if(device==null)
                    continue;
                User user = UserController.userController.userService.getUserByUserId(device.getUserId());
                if(user==null)
                    continue;
                JSONArray targetDevices = JSON.parseArray(user.getDevices());
                for(int i=0;i<targetDevices.size();i++) {
                    JSONObject json = targetDevices.getJSONObject(i);
                    if(json==null)
                        continue;
                    if (!json.containsKey("deviceId"))
                        continue;
                    if (json.getString("deviceId").equals(id)) {
                        new Thread(() -> refreshDevicesFor(deviceId)).start();
                        continue A;
                    }
                }
            }
        }
    }

    public static void refreshDevicesForUser(int userId) {
        synchronized (handles) {
            for (String deviceId : handles.keySet()) {
                Device device = UserController.userController.deviceService.getDeviceByDeviceId(deviceId);
                if(device==null)
                    continue;
                if(device.getUserId()!=userId)
                    continue;
                new Thread(() -> refreshDevicesFor(deviceId)).start();
            }
        }
    }

    public static void refreshDevicesFor(String deviceId) {
        Session session = handles.get(deviceId);
        if (session == null || !session.isOpen())
            return;
        Device device = UserController.userController.deviceService.getDeviceByDeviceId(deviceId);
        if (device == null)
            return;
        User user = UserController.userController.userService.getUserByUserId(device.getUserId());
        if (user == null)
            return;
        if (Utils.stringIsEmpty(user.getDevices()))
            return;
        JSONArray devices = new JSONArray();
        JSONArray targetDevices = JSON.parseArray(user.getDevices());
        for(int i=0;i<targetDevices.size();i++) {
            JSONObject json = targetDevices.getJSONObject(i);
            if (!json.containsKey("deviceId"))
                continue;
            String id = json.getString("deviceId");
            Device d = UserController.userController.deviceService.getDeviceByDeviceId(id);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("deviceId", id);
            jsonObject.put("connectId", json.getString("connectId"));
            jsonObject.put("connectPin", json.getString("connectPin"));
            jsonObject.put("status", Utils.getStatus(id, device.getVersion()));
            if (d == null) {
                jsonObject.put("name", "不存在该设备");
                devices.add(jsonObject);
                continue;
            }
            if (d.getDeviceInfo() == null) {
                jsonObject.put("name", "未知名称");
                devices.add(jsonObject);
                continue;
            }
            try {
                JSONObject deviceInfo = JSON.parseObject(d.getDeviceInfo());
                jsonObject.put("name", deviceInfo.getString("model"));
                devices.add(jsonObject);
            } catch (Exception e) {
                e.printStackTrace();
                jsonObject.put("name", "未知名称");
                devices.add(jsonObject);
            }
        }
        JSONObject result = new JSONObject();
        result.put("type", "devices");
        result.put("devices", devices);
        Utils.sendMessage(session, result.toString(), new Utils.OnSendFailedListener() {
            @Override
            public void onSendFailed(Session session, String message, IOException e) {
                //e.printStackTrace();
                try {
                    Handler.removeHandler(deviceId);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}
