package com.mj.lrp.service;

import com.mj.lrp.controller.UserController;
import com.mj.lrp.model.User;
import com.mj.lrp.server.Tick;

import java.util.HashMap;
import java.util.Map;

public class UserVipService {
    public static Map<Integer, Long> map = new HashMap<>();
    private static final Map<Integer, Long> lastUpdateMap = new HashMap<>();

    static {
        new Tick(10000, new Tick.OnTick() {
            @Override
            public void onTick() {
                for (int i : map.keySet()) {
                    User user = UserController.userController.userService.getUserByUserId(i);
                    if(user==null)
                        continue;
                    if(lastUpdateMap.containsKey(i) && user.getVip()!=lastUpdateMap.get(i)) {
                        lastUpdateMap.replace(i, user.getVip());
                        map.replace(i, user.getVip());
                        continue;
                    }
                    user.setVip(map.get(i));
                    if(lastUpdateMap.containsKey(i)) {
                        lastUpdateMap.replace(i, user.getVip());
                    } else {
                        lastUpdateMap.put(i, user.getVip());
                    }
                    UserController.userController.userService.update(user);
                }
            }
        }).start();
    }

    public static long get(int userId) {
        if (map.containsKey(userId)) {
            return map.get(userId);
        }
        User user = UserController.userController.userService.getUserByUserId(userId);
        if(user==null)
            return -1;
        map.put(userId, user.getVip());
        return user.getVip();
    }

    public static void insertOrUpdate(int userId, long vip) {
        if(map.containsKey(userId)) {
            map.replace(userId, vip);
            return;
        }
        map.put(userId, vip);
    }
}
