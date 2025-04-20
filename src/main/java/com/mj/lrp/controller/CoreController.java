package com.mj.lrp.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mj.lrp.model.*;
import com.mj.lrp.server.Handler;
import com.mj.lrp.service.UserVipService;
import com.mj.lrp.service.InfoService;
import com.mj.lrp.service.LoginInfoService;
import com.mj.lrp.service.SignInService;
import com.mj.lrp.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/core")
public class CoreController {
    @Autowired
    public InfoService infoService;

    @Autowired
    public SignInService signInService;

    @Autowired
    public LoginInfoService loginInfoService;

    public static CoreController coreController;

    @PostConstruct
    public void init() {
        coreController = this;
        coreController.infoService = infoService;
        coreController.signInService = signInService;
        coreController.loginInfoService = loginInfoService;
    }

    @ResponseBody
    @RequestMapping(value = "/getSignInInfo", produces = "application/json;charset=UTF-8")
    public String getSignInInfo() {
        JSONObject result = new JSONObject();
        result.put("signInInfo", infoService.getByDescription("人类签到时长").getContent());
        return result.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/update")
    public String update(HttpServletRequest request) {
        String edition = Utils.urlString(request.getParameter("edition"));
        String version = Utils.urlString(request.getParameter("version"));
        String abi = Utils.urlString(request.getParameter("abi"));
        JSONObject result = new JSONObject();
        String currentVersion = infoService.getByDescription("最新版本"+ edition).getContent();
        String currentVersionDescription = infoService.getByDescription("最新版本描述"+ edition).getContent();
        boolean force = infoService.getByDescription("强制更新"+ edition).getContent().equals("true");
        Info info = infoService.getByDescription(version+ edition);
        if (info != null) {
            force = info.getContent().equals("true");
        }
        info = infoService.getByDescription(version + "描述"+ edition);
        if (info != null) {
            currentVersionDescription = info.getContent();
        }
        boolean needUpdate = !version.equals(currentVersion);
        if (version.equals(infoService.getByDescription("预发布版本"+ edition).getContent())) {
            needUpdate = false;
        }
        result.put("version", currentVersion);
        result.put("description", currentVersionDescription);
        result.put("downloadUrl", infoService.getByDescription(edition+"更新下载地址"+abi).getContent());
        result.put("downloadUrlManual", infoService.getByDescription(edition+"版下载地址").getContent());
        result.put("isExamining", infoService.getByDescription("是否处于审核期").getContent().equals("true"));
        if (needUpdate)
            result.put("force", force);
        return result.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/signIn")
    public String signIn(HttpServletRequest request) {
        JSONObject result = new JSONObject();
        User user = Utils.getUser(request);
        if (user ==null) {
            result.put("errno", 1);
            return result.toString();
        }
        boolean signInned;
        synchronized (signInService) {
            signInned = signInService.getByUserId(String.valueOf(user.getUserId())) != null;
            if(!signInned) {
                signInService.insert(new SignIn(user.getUserId(), user.getUsername()));
            }
        }
        if(signInned) {
            result.put("errno", 2);
            return result.toString();
        }
        if(UserVipService.get(user.getUserId())<=System.currentTimeMillis()) {
            if (user.getVipType() == 1)
                user.setVipType(0);
            user.setVip(System.currentTimeMillis()+Long.parseLong(infoService.getByDescription("签到时长").getContent()));
        } else
            user.setVip(UserVipService.get(user.getUserId())+Long.parseLong(infoService.getByDescription("签到时长").getContent()));
        UserVipService.insertOrUpdate(user.getUserId(), user.getVip());
        result.put("errno", 0);
        return result.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/getPcUrl")
    public String getPcUrl() {
        return CoreController.coreController.infoService.getByDescription("电脑版下载地址").getContent();
    }

    @ResponseBody
    @RequestMapping(value = "/getAndroidUrl")
    public String getAndroidUrl() {
        return CoreController.coreController.infoService.getByDescription("安卓版下载地址").getContent();
    }

    @ResponseBody
    @RequestMapping(value = "/getMessageContent")
    public String getMessageContent(HttpServletRequest request) {
        String version = Utils.urlString(request.getParameter("version"));
        String edition = Utils.urlString(request.getParameter("edition"));
        if(version.equals("")) {
            return infoService.getByDescription("公告"+edition).getContent();
        } else {
            Info info = infoService.getByDescription("公告"+edition+version);
            if(info!=null) {
                return info.getContent();
            } else {
                return infoService.getByDescription("公告"+edition).getContent();
            }
        }
    }

    @ResponseBody
    @RequestMapping(value = "/getDeviceByConnectId")
    public String getDeviceByConnectId(HttpServletRequest request) {
        String connectId = Utils.urlString(request.getParameter("connectId"));
        if(Utils.stringIsEmpty(connectId)) {
            return null;
        }
        JSONObject result = new JSONObject();
        Device device = UserController.userController.deviceService.getDeviceByConnectId(connectId);
        if(device ==null) {
            result.put("name", "不存在的设备");
            result.put("status", DeviceInfo.STATUS_OFFLINE);
            result.put("os", DeviceInfo.OS_UNKNOWN);
            return result.toString();
        }
        if(device.getDeviceInfo()!=null) {
            JSONObject jsonObject = JSON.parseObject(device.getDeviceInfo());
            result.put("name", jsonObject.getString("model"));
            result.put("os", jsonObject.getInteger("operateSystem"));
        }
        result.put("deviceId", device.getDeviceId());
        result.put("status", Handler.containsConnectId(connectId)? DeviceInfo.STATUS_ONLINE: DeviceInfo.STATUS_OFFLINE);
        return result.toString();
    }
}