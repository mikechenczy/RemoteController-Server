package com.mj.lrp.util;

import com.mj.lrp.Const;

import java.io.IOException;
import java.net.InetAddress;

/**
 * @author Mike_Chen
 * @date 2023/8/19
 * @apiNote
 */
public class PingUtil {
    public static boolean isReachable(String ip) {
        try {
            InetAddress inet6Address = InetAddress.getByName(ip);
            return inet6Address.isReachable(Const.connectTimeOut);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
