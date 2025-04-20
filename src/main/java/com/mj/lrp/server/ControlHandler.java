package com.mj.lrp.server;

import com.mj.lrp.controller.UserController;
import com.mj.lrp.control.ControlService;
import com.mj.lrp.model.Control;
import com.mj.lrp.model.Device;

/**
 * @author Mike_Chen
 * @date 2023/6/24
 * @apiNote
 */
public class ControlHandler {
    public static synchronized void establishNewControl(String controlledDeviceId, String controllerDeviceId) {
        Control control = new Control();
        control.setControlId(ControlService.getRandomControlId());
        control.setControlledDeviceId(controlledDeviceId);
        control.setControllerDeviceId(controllerDeviceId);
        ControlService.createControl(control);
        Device device = UserController.userController.deviceService.getDeviceByDeviceId(controlledDeviceId);
        device.setControlId(control.getControlId());
        UserController.userController.deviceService.insertOrUpdate(device);
        device = UserController.userController.deviceService.getDeviceByDeviceId(controllerDeviceId);
        device.setControlId(control.getControlId());
        UserController.userController.deviceService.insertOrUpdate(device);
        control.notifyControlEstablished();
    }
}
