package com.android.pal.chat;


import android.support.multidex.MultiDexApplication;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by vijay on 4/1/18.
 */

public class vChatApplication extends MultiDexApplication {
  @Override
  public void onCreate() {
    super.onCreate();
    FirebaseDatabase.getInstance().setPersistenceEnabled(true);
//    DatabaseReference scoresRef = FirebaseDatabase.getInstance().getReference("message");
//    scoresRef.keepSynced(true);
  }
}
