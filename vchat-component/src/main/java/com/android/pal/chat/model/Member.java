package com.android.pal.chat.model;


import com.android.pal.chat.base.StaticConfig;

/**
 * Created by admirar on 1/7/18.
 */

public class Member {
  public String id;
  public String name = "";
  public String groupId;
  public String email;
  public String avatar;
  public String mobile;

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
