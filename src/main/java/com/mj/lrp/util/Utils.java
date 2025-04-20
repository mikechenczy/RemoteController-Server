package com.mj.lrp.util;

import com.mj.lrp.controller.CoreController;
import com.mj.lrp.controller.PayController;
import com.mj.lrp.controller.UserController;
import com.mj.lrp.model.DeviceInfo;
import com.mj.lrp.model.PayType;
import com.mj.lrp.model.User;
import com.mj.lrp.server.Handler;
import com.mj.lrp.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.Session;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.*;

public class Utils {
    public static boolean stringIsNull(String s) {
        return string(s).isEmpty();
    }

    public static boolean stringIsEmpty(String s) {
        return string(s).replaceAll(" ", "").replaceAll("\r", "").replaceAll("\n", "").isEmpty();
    }

    public static boolean stringsIsNull(String[] strings) {
        if(strings==null)
            return true;
        return stringsIsNull(Arrays.asList(strings));
    }

    public static boolean stringsIsNull(List<String> strings) {
        if(strings==null || strings.size()==0)
            return true;
        for (String s : strings)
            if(stringIsNull(s))
                return true;
        return false;
    }

    public static String string(String s) {
        return s==null?"":s;
    }

    public static String urlString(String s) {
        try {
            return URLDecoder.decode(string(s), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return s;
        }
    }

    public static int getRandomUserId(UserService userService) {
        int userId = new Random().nextInt(90000000)+10000000;
        if(userService.getUserByUserId(userId)!=null)
            return getRandomUserId(userService);
        return userId;
    }

    public static String getRandomNum(int digit) {
        String result = "";
        for(int i=0;i<digit;i++) {
            result = result + new Random().nextInt(10);
        }
        return result;
    }

    public static boolean check(String username, String password) {
        /*return !(username.contains(" ") || password.contains(" ") || username.equals("") || password.equals("") || username.contains(",") || username.contains("&")
                || username.contains("?") || password.contains("&") || password.contains("?") || username.contains("=") || password.contains("=") || username.contains("/")
                || password.contains("/") || username.contains("\\") || password.contains("\\"));*/
        return !(username.contains(" ") || password.contains(" ") || username.equals("") || password.equals("") || username.length()>=15 || password.length()>=20);
    }

    public static boolean check(String password) {
        return !(password.contains(" ") || password.equals("") || password.length()>=20);
    }

    public static boolean isEmail(String str) {
        boolean isEmail = false;
        String expr = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})$";

        if (str.matches(expr)) {
            isEmail = true;
        }
        if(isEmail) {
            List<String> availableEmails = Arrays.asList(CoreController.coreController.infoService.getByDescription("支持的邮箱").getContent().split(";"));
            for (String availableEmail : availableEmails) {
                if(str.endsWith(availableEmail)) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    public static String encodeString(String s) {
        try {
            return URLEncoder.encode(s, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return s;
        }
    }

    public static Integer fileToFileId(File file) {
        if(file==null)
            return null;
        try {
            return Integer.parseInt(file.getName());
        }catch (Exception e) {
            return null;
        }
    }

    public static List<Integer> filesToFileIds(List<File> files) {
        if(files==null)
            return null;
        List<Integer> result = new ArrayList<>();
        for(File file : files) {
            result.add(fileToFileId(file));
        }
        return result;
    }

    public static boolean checkFileName(String fileName) {
        return fileName != null && (!(fileName.contains("\\") || fileName.contains("/") || fileName.contains(":") || fileName.contains("*") || fileName.contains("?")
                || fileName.contains("\"") || fileName.contains("<") || fileName.contains(">") || fileName.contains("|") || fileName.equals("")));
    }

    public static boolean isUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if(session==null)
            return false;
        else {
            Object o = session.getAttribute("user");
            return o instanceof User && (UserController.userController.userService.getUserByUserId(((User) o).getUserId()) != null);
        }
    }
    public static User getUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if(session==null)
            return null;
        else {
            Object o = session.getAttribute("user");
            return (o instanceof User && (UserController.userController.userService.getUserByUserId(((User) o).getUserId()) != null))? (User) o :null;
        }
    }

    public static long getVipByPayType(int payType) {
        PayType payment = PayController.payController.payTypeService.getByPayType(payType);
        if(payment==null)
            return -1;
        return payment.getVip();
    }

    public static String getDescribeByPayType(String payType) {
        try {
            return getDescribeByPayType(Integer.parseInt(payType));
        } catch (NumberFormatException e) {
            return "Vip";
        }
    }

    public static String getDescribeByPayType(int payType) {
        PayType payment = PayController.payController.payTypeService.getByPayType(payType);
        if(payment==null)
            return "Vip";
        return payment.getHumanizedVip();
    }

    public static String getPriceByPayType(String payType) {
        try {
            return getPriceByPayType(Integer.parseInt(payType));
        } catch (NumberFormatException e) {
            return "0";
        }
    }

    public static String getPriceByPayType(int payType) {
        PayType payment = PayController.payController.payTypeService.getByPayType(payType);
        if(payment==null)
            return "0";
        return payment.getPrice();
    }

    public static long getTomorrowZeroMillis() {
        long current = System.currentTimeMillis();// 当前时间毫秒数
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return (calendar.getTimeInMillis()- current);
    }

    public static int getStatus(String id, String version) {
        if(!Handler.handles.containsKey(id))
            return DeviceInfo.STATUS_OFFLINE;
        if(Handler.enabledDevices.contains(id))
            return DeviceInfo.STATUS_ONLINE;
        return DeviceInfo.STATUS_NOT_ENABLED;
    }

    public static abstract class OnSendFailedListener {
        public abstract void onSendFailed(Session session, String message, IOException e);
    }

    public static void sendMessageSync(Session session, String message) throws IOException {
        if (session==null)
            return;
        synchronized (session) {
            if(session.isOpen()) {
                session.getBasicRemote().sendText(message);
            }
        }
    }

    public static void sendBytesSync(Session session, ByteBuffer bytes) throws IOException {
        if (session==null)
            return;
        synchronized (session) {
            if(session.isOpen()) {
                session.getBasicRemote().sendBinary(bytes);
            }
        }
    }

    public static void sendMessage(Session session, String message, OnSendFailedListener onSendFailedListener) {
        if (session==null)
            return;
        new Thread(() -> {
            synchronized (session) {
                if(session.isOpen()) {
                    try {
                        session.getBasicRemote().sendText(message);
                    } catch (IOException e) {
                        if (onSendFailedListener != null)
                            onSendFailedListener.onSendFailed(session, message, e);
                    }
                }
            }
        }).start();
    }

    public static String formatFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString;
        String wrongSize="0B";
        if(fileS==0){
            return wrongSize;
        }
        if (fileS < 1024){
            fileSizeString = df.format((double) fileS) + "B";
        }
        else if (fileS < 1048576){
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        }
        else if (fileS < 1073741824){
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        }
        else{
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }

    public static String getHumanBandwidth(long bandwidth) {
        return formatFileSize(bandwidth);
    }
}
