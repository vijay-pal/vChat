package com.android.pal.chat.model;

/**
 * Created by vijay on 9/1/18.
 */

public class ChatRoom {
    public String id;
    public String roomId;
    public String name = "";
    public String email = "";
    public String avatar;
    public String admin;
    public String status = "";
    public long timestamp;
    public boolean isGroup;

    public boolean containsIgnoreCare(String s) {
        s = s.toUpperCase();
        return name.toUpperCase().contains(s) || email.toUpperCase().contains(s);
    }
}
