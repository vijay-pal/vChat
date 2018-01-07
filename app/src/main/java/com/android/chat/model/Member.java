package com.android.chat.model;

import com.android.chat.data.StaticConfig;

/**
 * Created by admirar on 1/7/18.
 */

public class Member {
    public String id;
    public String name;
    public String groupId;
    public String email;
    public String avatar;

    @Override
    public String toString() {
        if (StaticConfig.UID.equals(id)) {
            return "You";
        }
        return name;
    }

    public boolean isAdmin(String adminId) {
        return adminId != null && adminId.equals(id);
    }
}
