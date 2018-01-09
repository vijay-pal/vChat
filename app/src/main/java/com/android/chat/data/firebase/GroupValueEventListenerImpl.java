package com.android.chat.data.firebase;

import android.content.Context;

import com.android.chat.model.ChatRoom;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by vijay on 9/1/18.
 */

public class GroupValueEventListenerImpl implements ValueEventListener {

  private GroupRefreshCompletedListener mListener;
  private List<ChatRoom> groups;
  private Context context;

  public GroupValueEventListenerImpl(GroupRefreshCompletedListener mListener, Context context) {
    this.mListener = mListener;
    this.groups = groups;
    this.context = context;
  }

  public GroupValueEventListenerImpl(GroupRefreshCompletedListener mListener, List<ChatRoom> groups, Context context) {
    this.mListener = mListener;
    this.groups = groups;
    this.context = context;
  }

  @Override
  public void onDataChange(DataSnapshot dataSnapshot) {
    if (dataSnapshot.getValue() != null) {
      HashMap mapListGroup = (HashMap) dataSnapshot.getValue();
      Iterator iterator = mapListGroup.keySet().iterator();
      while (iterator.hasNext()) {
        String idGroup = (String) mapListGroup.get(iterator.next().toString());
        final ChatRoom newGroup = new ChatRoom();
        newGroup.id = idGroup;
        newGroup.roomId = idGroup;
        groups.add(newGroup);

      }
      getGroupInfo(0);
    } else if (mListener != null) {
      mListener.fetchFriend();
    }
  }

  @Override
  public void onCancelled(DatabaseError databaseError) {

  }

  private void getGroupInfo(final int indexGroup) {
    if (indexGroup >= groups.size()) {
      if (mListener != null) {
        mListener.fetchFriend();
      }
    } else {
      FirebaseDatabase.getInstance().getReference().child("group/" + groups.get(indexGroup).id).addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
          if (dataSnapshot.getValue() != null) {
            HashMap mapGroup = (HashMap) dataSnapshot.getValue();
            HashMap mapGroupInfo = (HashMap) mapGroup.get("groupInfo");
            groups.get(indexGroup).name = (String) mapGroupInfo.get("name");
            groups.get(indexGroup).admin = (String) mapGroupInfo.get("admin");
            groups.get(indexGroup).avatar = (String) mapGroupInfo.get("avatar");
          }
//          GroupDB.getInstance(context).addGroup(groups.get(indexGroup));
          getGroupInfo(indexGroup + 1);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
          if (mListener != null) {
            mListener.onCancelled();
          }
        }
      });
    }
  }

  public interface GroupRefreshCompletedListener {

    void onCompleted(List<ChatRoom> groups);

    void onCancelled();

    void fetchFriend();
  }
}
