package com.mj.lrp.controller;

import com.alibaba.fastjson.JSONObject;
import com.alipay.easysdk.factory.Factory;
import com.mj.lrp.model.Pay;
import com.mj.lrp.model.PayLog;
import com.mj.lrp.model.PayType;
import com.mj.lrp.model.User;
import com.mj.lrp.service.UserVipService;
import com.mj.lrp.service.PayLogService;
import com.mj.lrp.service.PayService;
import com.mj.lrp.service.PayTypeService;
import com.mj.lrp.util.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Autowired
    public PayService payService;
    @Autowired
    public PayLogService payLogService;
    @Autowired
    public PayTypeService payTypeService;

    public static PayController payController;

    @PostConstruct
    public void init() {
        payController = this;
        payController.payService = payService;
        payController.payLogService = payLogService;
        payController.payTypeService = payTypeService;
    }

    @ResponseBody
    @RequestMapping(value = "/getPayTypes")
    public String getPayTypes() {
        List<PayType> payTypes = payTypeService.getAll();
        JSONObject result = new JSONObject();
        result.put("payTypes", payTypes);
        return result.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/getOrderInfo")
    public String getOrderInfo(HttpServletRequest request) {
        return payMethod(request, PAY_ORDER_INFO);
    }

    public static final int PAY_TRADE = 0;
    public static final int PAY_ORDER_INFO = 1;

    public String payMethod(HttpServletRequest request, int methodType) {
        if(Utils.isUser(request)) {
            String payType = Utils.urlString(request.getParameter("payType"));
            int type;
            try {
                type = Integer.parseInt(payType);
            } catch (NumberFormatException e) {
                return "";
            }
            if(payTypeService.getByPayType(type)==null)
                return "";
            String payNo = String.valueOf(System.currentTimeMillis()).substring(0, 8) +
                    UUID.randomUUID().toString().replaceAll("-", "");
            String price = Utils.getPriceByPayType(type);
            String describe = Utils.getDescribeByPayType(type);
            User user = Utils.getUser(request);
            payService.insert(new Pay(payNo, type, user.getUserId()));
            new Thread(() -> {
                try {
                    Thread.sleep(Long.parseLong(CoreController.coreController.infoService.getByDescription("支付过期时间").getContent()));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (payService) {
                    Pay pay = payService.getByPayNo(payNo);
                    if(pay!=null) {
                        try {
                            if ("TRADE_SUCCESS".equalsIgnoreCase(Factory.Payment.Common().query(payNo).getTradeStatus())) {
                                long vip = pay.getVip();
                                if(vip>0) {
                                    user.setVip(UserVipService.get(user.getUserId()) > 0 ? (UserVipService.get(user.getUserId()) + vip) : vip);
                                    user.setVipType(1);
                                    UserVipService.insertOrUpdate(user.getUserId(), user.getVip());
                                }
                                payService.delete(payNo);
                                payLogService.insert(new PayLog(pay));
                            } else {
                                Factory.Payment.Common().close(payNo);
                                payService.delete(payNo);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
            switch (methodType) {
                case PAY_TRADE:
                    try {
                        return Factory.Payment.FaceToFace().preCreate(describe, payNo, price).getQrCode()+payNo;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                case PAY_ORDER_INFO:
                    try {
                        return Factory.Payment.App().pay(describe, payNo, price).getBody() + payNo;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                default:
                    return "";
            }
        }
        System.out.println("notUser");
        return "";
    }

    @ResponseBody
    @RequestMapping(value = "/query", produces = "application/json;charset=UTF-8")
    public String query(HttpServletRequest request) {
        String payNo = Utils.urlString(request.getParameter("payNo"));
        JSONObject result = new JSONObject();
        int errno = 0;
        if(Utils.stringIsEmpty(payNo)) {
            errno = 2;
        } else {
            synchronized (payService) {
                Pay pay = payService.getByPayNo(payNo);
                if (pay == null)
                    errno = 2;
                else {
                    boolean success = false;
                    try {
                        success = "TRADE_SUCCESS".equalsIgnoreCase(Factory.Payment.Common().query(payNo).getTradeStatus());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (success) {
                        User user = UserController.userController.userService.getUserByUserId(pay.getUserId());
                        long vip = pay.getVip();
                        if (vip > 0) {
                            user.setVip(UserVipService.get(user.getUserId()) > 0 ? (UserVipService.get(user.getUserId()) + vip) : vip);
                            user.setVipType(1);
                            UserVipService.insertOrUpdate(user.getUserId(), user.getVip());
                        }
                        payService.delete(payNo);
                        payLogService.insert(new PayLog(pay));
                    }
                    result.put("result", success);
                }
            }
        }
        result.put("errno", errno);
        return result.toString();
    }

    public static void checkPay(User user) {
        new Thread(() -> {
            synchronized (payController.payService) {
                List<Pay> pays = payController.payService.getPayByUserId(user.getUserId());
                for(Pay pay : pays) {
                    try {
                        if ("TRADE_SUCCESS".equalsIgnoreCase(Factory.Payment.Common().query(pay.getPayNo()).getTradeStatus())) {
                            long vip = pay.getVip();
                            user.setVip(UserVipService.get(user.getUserId()) > 0 ? (UserVipService.get(user.getUserId()) + vip) : vip);
                            user.setVipType(1);
                            UserVipService.insertOrUpdate(user.getUserId(), user.getVip());
                            payController.payService.delete(pay.getPayNo());
                            payController.payLogService.insert(new PayLog(pay));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}