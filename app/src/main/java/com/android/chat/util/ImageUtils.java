package com.android.chat.util;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Base64;
import android.webkit.MimeTypeMap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;


public class ImageUtils {

  public static final int AVATAR_WIDTH = 128;
  public static final int AVATAR_HEIGHT = 128;

  /**
   * Bo tròn ảnh avatar
   *
   * @param context
   * @param src     ảnh dạng bitmap
   * @return RoundedBitmapDrawable là đầu vào cho hàm setImageDrawable()
   */
  public static RoundedBitmapDrawable roundedImage(Context context, Bitmap src) {
        /*Bo tròn avatar*/
    Resources res = context.getResources();
    RoundedBitmapDrawable dr =
      RoundedBitmapDrawableFactory.create(res, src);
    dr.setCornerRadius(Math.max(src.getWidth(), src.getHeight()) / 2.0f);

    return dr;
  }

  /**
   * Đối với ảnh hình chữ nhật thì cần cắt ảnh theo hình vuông và lấy phần tâm
   * ảnh để khi đặt làm avatar sẽ không bị méo
   *
   * @param srcBmp
   * @return
   */
  public static Bitmap cropToSquare(Bitmap srcBmp) {
    Bitmap dstBmp = null;
    if (srcBmp.getWidth() >= srcBmp.getHeight()) {

      dstBmp = Bitmap.createBitmap(
        srcBmp,
        srcBmp.getWidth() / 2 - srcBmp.getHeight() / 2,
        0,
        srcBmp.getHeight(),
        srcBmp.getHeight()
      );

    } else {
      dstBmp = Bitmap.createBitmap(
        srcBmp,
        0,
        srcBmp.getHeight() / 2 - srcBmp.getWidth() / 2,
        srcBmp.getWidth(),
        srcBmp.getWidth()
      );
    }

    return dstBmp;
  }

  /**
   * Convert ảnh dạng bitmap ra String base64
   *
   * @param imgBitmap
   * @return
   */
  public static String encodeBase64(Bitmap imgBitmap) {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    imgBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
    byte[] byteArray = byteArrayOutputStream.toByteArray();
    return Base64.encodeToString(byteArray, Base64.DEFAULT);
  }

  /**
   * Làm giảm số điểm ảnh xuống để tránh lỗi Firebase Database OutOfMemory
   *
   * @param is        anh dau vao
   * @param reqWidth  kích thước chiều rộng sau khi giảm
   * @param reqHeight kích thước chiều cao sau khi giảm
   * @return
   */
  public static Bitmap makeImageLite(InputStream is, int width, int height,
                                     int reqWidth, int reqHeight) {
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {
      final int halfHeight = height / 2;
      final int halfWidth = width / 2;

      // Calculate the largest inSampleSize value that is a power of 2 and keeps both
      // height and width larger than the requested height and width.
      while ((halfHeight / inSampleSize) >= reqHeight
        && (halfWidth / inSampleSize) >= reqWidth) {
        inSampleSize *= 2;
      }
    }

    // Calculate inSampleSize
    final BitmapFactory.Options options = new BitmapFactory.Options();
    options.inSampleSize = inSampleSize;

    // Decode bitmap with inSampleSize set
    options.inJustDecodeBounds = false;
    return BitmapFactory.decodeStream(is, null, options);
  }


  public static InputStream convertBitmapToInputStream(Bitmap bitmap) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
    byte[] bitmapdata = bos.toByteArray();
    ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);
    return bs;
  }

  @SuppressLint("NewApi")
  public static String getPath(final Context context, final Uri uri) {

    final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

    // DocumentProvider
    if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
      // ExternalStorageProvider
      if (isExternalStorageDocument(uri)) {
        final String docId = DocumentsContract.getDocumentId(uri);
        final String[] split = docId.split(":");
        final String type = split[0];

        if ("primary".equalsIgnoreCase(type)) {
          return Environment.getExternalStorageDirectory() + "/"
            + split[1];
        }

        // TODO handle non-primary volumes
      }
      // DownloadsProvider
      else if (isDownloadsDocument(uri)) {

        final String id = DocumentsContract.getDocumentId(uri);
        final Uri contentUri = ContentUris.withAppendedId(
          Uri.parse("content://downloads/public_downloads"),
          Long.valueOf(id));

        return getDataColumn(context, contentUri, null, null);
      }
      // MediaProvider
      else if (isMediaDocument(uri)) {
        final String docId = DocumentsContract.getDocumentId(uri);
        final String[] split = docId.split(":");
        final String type = split[0];

        Uri contentUri = null;
        if ("image".equals(type)) {
          contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else if ("video".equals(type)) {
          contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else if ("audio".equals(type)) {
          contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        final String selection = "_id=?";
        final String[] selectionArgs = new String[]{split[1]};

        return getDataColumn(context, contentUri, selection,
          selectionArgs);
      }
    }
    // MediaStore (and general)
    else if ("content".equalsIgnoreCase(uri.getScheme())) {
      return getDataColumn(context, uri, null, null);
    }
    // File
    else if ("file".equalsIgnoreCase(uri.getScheme())) {
      return uri.getPath();
    }

    return null;
  }

  public static boolean isExternalStorageDocument(Uri uri) {
    return "com.android.externalstorage.documents".equals(uri
      .getAuthority());
  }

  public static boolean isDownloadsDocument(Uri uri) {
    return "com.android.providers.downloads.documents".equals(uri
      .getAuthority());
  }

  public static boolean isMediaDocument(Uri uri) {
    return "com.android.providers.media.documents".equals(uri
      .getAuthority());
  }

  public static String getDataColumn(Context context, Uri uri,
                                     String selection, String[] selectionArgs) {

    Cursor cursor = null;
    final String column = "_data";
    final String[] projection = {column};

    try {
      cursor = context.getContentResolver().query(uri, projection,
        selection, selectionArgs, null);
      if (cursor != null && cursor.moveToFirst()) {
        final int column_index = cursor.getColumnIndexOrThrow(column);
        return cursor.getString(column_index);
      }
    } finally {
      if (cursor != null)
        cursor.close();
    }
    return null;
  }

  public static String getFileName(String filePath) {
    if (filePath == null || !filePath.contains(File.separator)) {
      return null;
    }
    return filePath.substring(filePath.lastIndexOf(File.separator) + 1);
  }

  public static String getChatFolder(String leafDirectory) {
    File myDirectory = new File(Environment.getExternalStorageDirectory(),
      File.separator + "vChat" + File.separator + leafDirectory);

    if (!myDirectory.exists()) {
      myDirectory.mkdirs();
    }
    return myDirectory.getAbsolutePath();
  }

  public static String getBase64String(Context context, Uri uri) {
    try {
      InputStream inputStream = context.getContentResolver().openInputStream(uri);
      Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
      bitmap = Bitmap.createScaledBitmap(bitmap, 50, 50, false);
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
      byte[] byteArray = byteArrayOutputStream.toByteArray();
      return Base64.encodeToString(byteArray, Base64.DEFAULT);
    } catch (FileNotFoundException e) {
      return null;
    }
  }

  public static Bitmap getBitmap(String base64) {
    byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
    return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
  }
}
