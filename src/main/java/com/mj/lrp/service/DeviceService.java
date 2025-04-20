package com.mj.lrp.service;

import com.mj.lrp.dao.DeviceDao;
import com.mj.lrp.model.Device;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service("deviceService")
public class DeviceService {

    @Autowired
    private DeviceDao deviceDao;

    public Device getDeviceByDeviceId(String deviceId) {
        return deviceDao.getDeviceByDeviceId(deviceId);
    }

    public Device getDeviceByUserId(int userId) {
        return deviceDao.getDeviceByUserId(userId);
    }

    public Device getDeviceByConnectId(String connectId) {
        return deviceDao.getDeviceByConnectId(connectId);
    }

    public int delete(String deviceId) {
        return deviceDao.delete(deviceId);
    }

    public int insert(Device device) {
        return deviceDao.insert(device);
    }

    public int update(Device device) {
        return deviceDao.update(device);
    }

    public List<Device> getAll() {
        return deviceDao.getAll();
    }

    public int deleteByUserId(int userId) {
        return deviceDao.deleteByUserId(userId);
    }

    public int insertOrUpdate(Device device) {
        if(getDeviceByDeviceId(device.getDeviceId())!=null) {
            return update(device);
        }
        return insert(device);
    }

    public synchronized String generateConnectId() {
        return generateConnectId(getAll());
    }

    private synchronized String generateConnectId(List<Device> deviceList) {
        String id = String.valueOf(10000+new Random().nextInt(90000));
        for(Device device : deviceList) {
            if(device.getConnectId().equals(id))
                return generateConnectId(deviceList);
        }
        return id;
    }

    public synchronized String generateConnectPin() {
        return generateConnectPin(getAll());
    }

    private synchronized String generateConnectPin(List<Device> deviceList) {
        String id = String.valueOf(10000+new Random().nextInt(90000));
        for(Device device : deviceList) {
            if(device.getConnectPin().equals(id))
                return generateConnectId(deviceList);
        }
        return id;
    }
}