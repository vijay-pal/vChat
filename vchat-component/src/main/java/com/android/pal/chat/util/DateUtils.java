package com.android.pal.chat.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by vijay on 4/1/18.
 */

public class DateUtils {

  public static final String FORMAT_hh_mm_a = "hh.mm a";

  public static String format(long dateTime, String format) {
    SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
    Date date = new Date(dateTime);
    return sdf.format(date);
  }
}
