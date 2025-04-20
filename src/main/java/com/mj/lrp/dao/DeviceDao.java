package com.mj.lrp.dao;

import com.mj.lrp.model.Device;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DeviceDao {

    int insert(Device device);

    int update(Device device);

    int delete(@Param("deviceId") String deviceId);

    int deleteByUserId(@Param("userId") int userId);

    Device getDeviceByDeviceId(@Param("deviceId") String deviceId);

    Device getDeviceByUserId(@Param("userId") int userId);

    Device getDeviceByConnectId(@Param("connectId") String connectId);

    List<Device> getAll();
}