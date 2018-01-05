package com.android.chat.util;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

/**
 * Created by vijay on 4/1/18.
 */

public class VideoUtils {
  public static String getVideoPath(Context context, Uri uri) {
    String picturePath = ImageUtils.getPath(context, uri);
    return picturePath;
  }

  public static String getBase64StringThumbnail(String picturePath) {
    Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(picturePath, MediaStore.Video.Thumbnails.MICRO_KIND);
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
    byte[] byteArray = byteArrayOutputStream.toByteArray();
    return Base64.encodeToString(byteArray, Base64.DEFAULT);
  }
}
