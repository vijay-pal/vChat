package com.android.pal.chat.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by admirar on 1/7/18.
 */

public class GlideUtils {

  public static void setRoundedDrawable(final Context context, String source, final ActionBar target) {

    Glide.with(context).load(source)
      .asBitmap().centerCrop().into(new SimpleTarget<Bitmap>(70, 70) {
      @Override
      public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
        RoundedBitmapDrawable circularBitmapDrawable =
          RoundedBitmapDrawableFactory.create(context.getResources(), resource);
        circularBitmapDrawable.setCircular(true);
        target.setLogo(circularBitmapDrawable);
      }
    });
  }

  public static void display(final Context context, String source, final ImageView imageView, int defaultRes) {
    Glide.with(context)
      .load(source)
      .placeholder(defaultRes)
      .error(defaultRes)
      .fitCenter()
      .into(imageView);
  }

  public static void display(final Context context, String source, final ImageView imageView,
                             String name, int defaultRes) {
    Glide.with(context)
      .load(source)
      .placeholder(createImage(context, 60, 60, name, defaultRes))
      .error(defaultRes)
      .fitCenter()
      .into(imageView);
  }

  public static void display(final Context context, DatabaseReference reference, final ImageView imageView,
                             String name, final int defaultRes) {
    imageView.setImageDrawable(createImage(context, 60, 60, name, defaultRes));

    reference.addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        if (dataSnapshot.getValue() != null) {
          display(context, dataSnapshot.getValue().toString(), imageView, defaultRes);
        }
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {

      }
    });
  }

  public static void display(final Context context, DatabaseReference reference, final ImageView imageView, final int defaultRes) {
    imageView.setImageResource(defaultRes);

    reference.addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        if (dataSnapshot.getValue() != null) {
          display(context, dataSnapshot.getValue().toString(), imageView, defaultRes);
        }
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {

      }
    });
  }

  public static Drawable createImage(Context context, int width, int height, String name, int defaultRes) {
    if (TextUtils.isEmpty(name)) {
      return ContextCompat.getDrawable(context, defaultRes);
    }

    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    Paint paint2 = new Paint();
    paint2.setColor(name.hashCode());
    canvas.drawRect(0F, 0F, (float) width, (float) height, paint2);
    Paint paint = new Paint();
    paint.setColor(Color.WHITE);
    paint.setTextSize(30);
    paint.setTextScaleX(1);
    canvas.drawText(String.valueOf(name.charAt(0)), width / 2 - 10, height / 2 + 10, paint);
    return new BitmapDrawable(context.getResources(), bitmap);
  }
}
