package com.example.arc.bestcontacts.utils;

import com.example.arc.bestcontacts.models.TinyDB;
import com.example.arc.bestcontacts.models.User;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Created by arc on 16/06/17.
 */

public class Global {

    public Global()
    {
        this.userList = null;
    }

    private final static String SecurityKey = "GN0aN0BAeuHGGmHdRUzcegjD1pZtS2gcIh1Lc6aLIN7yesNGMu";
    private final static String SecurityNameKey = "androidKey";

    private static List<User> userList;

    public static List<User> getUserList() {
        return userList;
    }

    public static void setUserList(List<User> userList) {
        Global.userList = userList;
    }

    private static User loggedUser;
    public static void setLoggedUserPassword(String pass){ Global.loggedUser.setPassword(pass);}
    public static void deleteLoggedUser() { loggedUser = null;}
    public static User getLoggedUser() {
        return loggedUser;
    }

    public static void setLoggedUser(User loggedUser) {
        Global.loggedUser = loggedUser;
    }

    public static String getSecurityKey() { return SecurityKey; }
    public static String getSecurityNameKey() { return SecurityNameKey; }

    public static String md5(String s) {
        try {
            MessageDigest digest = java.security.MessageDigest
                    .getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
