package com.android.pal.chat.base;

/**
 * Created by vijay on 18/1/18.
 */

public class VChatSettings {

  public static void enableGlobalSearch(boolean b) {
    StaticConfig.IS_ENABLE_GLOBAL_SEARCH = b;
  }

  public static void enableCreateGroup(boolean b) {
    StaticConfig.IS_ENABLE_CREATE_GROUP = b;
  }

  public static void enableShowProfile(boolean b) {
    StaticConfig.IS_ENABLE_MY_PROFILE = b;
  }

  public static void enableSearch(boolean b) {
    StaticConfig.IS_ENABLE_SEARCH_OPTION = b;
  }

  public static void enableAboutApp(boolean b) {
    StaticConfig.IS_ENABLE_ABOUT_OPTION = b;
  }

  public static void enableOneToOneChatRoom(boolean b) {
    StaticConfig.IS_EMABLE_ONE_ONE_CHAT_ROOM = b;
  }
}
