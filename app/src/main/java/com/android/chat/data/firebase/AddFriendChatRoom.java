package com.android.chat.data.firebase;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.NonNull;

import com.android.chat.R;
import com.android.chat.data.ChatRoomDB;
import com.android.chat.data.StaticConfig;
import com.android.chat.model.ChatRoom;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

/**
 * Created by vijay on 10/1/18.
 */

public class AddFriendChatRoom {
  private Context context;
  private ChatRoom chatRoom;
  private FriendAddedListener mListener;

  private ProgressDialog progressDialog;

  public AddFriendChatRoom(Context context, ChatRoom chatRoom, FriendAddedListener mListener) {
    this.context = context;
    this.chatRoom = chatRoom;
    this.mListener = mListener;
    progressDialog = new ProgressDialog(context);
    addFriend(chatRoom.id, true);
  }

  private void addFriend(final String friendId, final boolean isFriendId) {
    progressDialog.show();
    FirebaseDatabase.getInstance().getReference().child("friend/" + (isFriendId ? StaticConfig.UID : friendId))
      .push().setValue(isFriendId ? friendId : StaticConfig.UID)
      .addOnCompleteListener(new OnCompleteListener<Void>() {
        @Override
        public void onComplete(@NonNull Task<Void> task) {
          if (task.isSuccessful()) {
            if (isFriendId) {
              addFriend(friendId, false);
            } else {
              FirebaseDatabase.getInstance().getReference().child("friend/" + StaticConfig.UID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                  @Override
                  public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                      HashMap mapListGroup = (HashMap) dataSnapshot.getValue();
                      for (Object key : mapListGroup.keySet()) {
                        if (friendId.equals(mapListGroup.get(key))) {
                          chatRoom.roomId = (String) key;
                          if (mListener != null) {
                            ChatRoomDB.getInstance(context).addChatRoom(chatRoom);
                            mListener.onAdded(chatRoom);
                          }
                        }
                      }
                    }
                    progressDialog.dismiss();
                  }

                  @Override
                  public void onCancelled(DatabaseError databaseError) {
                    progressDialog.dismiss();
                    if (mListener != null) {
                      mListener.onFailed();
                    }
                  }
                });
            }
          } else {
            progressDialog.dismiss();
          }
        }
      })
      .addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
          progressDialog.dismiss();
        }
      });
  }

  public interface FriendAddedListener {
    void onAdded(ChatRoom chatRoom);

    void onFailed();
  }
}
