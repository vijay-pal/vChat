package com.android.pal.chat.data.firebase;

import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.android.pal.chat.R;
import com.android.pal.chat.ui.adapter.ConversationAdapter;
import com.android.pal.chat.util.DateUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

/**
 * Created by admirar on 8/2/18.
 */

public class GroupMessage {

  public void setMessage(String groupId, final TextView message, final TextView time) {
    message.setText("");
    time.setText("");
    FirebaseDatabase.getInstance().getReference().child("message/" + groupId).limitToLast(1)
      .addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
          HashMap hashMap = (HashMap) dataSnapshot.getValue();
          if (hashMap != null) {
            for (Object key : hashMap.keySet()) {
              HashMap map = (HashMap) hashMap.get(key);
              Log.i("DATA", "DATA::" + map);
              String type = (String) map.get("type");
              if (!TextUtils.isEmpty(type)) {
                if (type.equalsIgnoreCase(ConversationAdapter.MESSAGE_TYPE_TEXT)) {
                  message.setText((String) map.get("text"));
                  message.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);
                } else if (type.equalsIgnoreCase(ConversationAdapter.MESSAGE_TYPE_IMAGE)) {
                  message.setCompoundDrawablesRelativeWithIntrinsicBounds(ContextCompat.getDrawable(message.getContext(), R.drawable.ic_photo_camera_black_24dp), null, null, null);
                  message.setText("Photo");
                } else if (type.equalsIgnoreCase(ConversationAdapter.MESSAGE_TYPE_VIDEO)) {
                  message.setCompoundDrawablesRelativeWithIntrinsicBounds(ContextCompat.getDrawable(message.getContext(), R.drawable.ic_video_library_black_24dp), null, null, null);
                  message.setText("Video");
                } else {
                  message.setText("");
                  message.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);
                }
              }
              time.setText(DateUtils.format((long) map.get("timestamp"), DateUtils.FORMAT_dd_MM_YY));
              break;
            }
          }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
      });
  }
}
