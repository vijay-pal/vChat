package com.android.pal.chat.base;


public class StaticConfig {
  public static int REQUEST_CODE_REGISTER = 2000;
  public static String STR_EXTRA_ACTION_LOGIN = "login";
  public static String STR_EXTRA_ACTION_RESET = "resetpass";
  public static String STR_EXTRA_ACTION = "action";
  public static String STR_EXTRA_USERNAME = "username";
  public static String STR_EXTRA_PASSWORD = "password";
  public static String STR_DEFAULT_BASE64 = "default";
  public static String UID = "";
  //TODO only use this UID for debug mode
//    public static String UID = "6kU0SbJPF5QJKZTfvW1BqKolrx22";
  public static String INTENT_KEY_CHAT_ROOM = "friendname";
  public static String INTENT_KEY_CHAT_AVATAR = "avatar";
  public static String INTENT_KEY_CHAT_ID = "friendid";
  public static String INTENT_KEY_CHAT_ROOM_ID = "roomid";
  public static String INTENT_KEY_CHAT_IS_GROUP = "isgroup";

  public static long TIME_TO_REFRESH = 10 * 1000;
  public static long TIME_TO_OFFLINE = 2 * 60 * 1000;

  //Custom Settings
  public static boolean IS_ENABLE_GLOBAL_SEARCH = true;
  public static boolean IS_ENABLE_CREATE_GROUP = true;
  public static boolean IS_ENABLE_MY_PROFILE = true;
  public static boolean IS_ENABLE_SEARCH_OPTION = true;
  public static boolean IS_ENABLE_ABOUT_OPTION = true;
  public static boolean IS_EMABLE_ONE_ONE_CHAT_ROOM = true;
}
