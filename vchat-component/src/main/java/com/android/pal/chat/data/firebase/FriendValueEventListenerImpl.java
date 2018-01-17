package com.android.pal.chat.data.firebase;

import android.content.Context;

import com.android.pal.chat.model.ChatRoom;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

/**
 * Created by vijay on 9/1/18.
 */

public class FriendValueEventListenerImpl implements ValueEventListener {

    private GroupValueEventListenerImpl.GroupRefreshCompletedListener mListener;
    private List<ChatRoom> friends;
    private Context context;

    public FriendValueEventListenerImpl(GroupValueEventListenerImpl.GroupRefreshCompletedListener mListener, List<ChatRoom> friends, Context context) {
        this.mListener = mListener;
        this.friends = friends;
        this.context = context;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if (dataSnapshot.getValue() != null) {
            HashMap mapListGroup = (HashMap) dataSnapshot.getValue();
            for (Object key : mapListGroup.keySet()) {
                final ChatRoom newFriend = new ChatRoom();
                newFriend.id = (String) mapListGroup.get(key);
                newFriend.roomId = (String) key;
                friends.add(newFriend);
            }
            getUserInfo(0);
        } else if (mListener != null) {
            mListener.onCompleted(friends);
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

    private void getUserInfo(final int indexFriend) {
        if (indexFriend >= friends.size()) {
            if (mListener != null) {
                mListener.onCompleted(friends);
            }
        } else {
            FirebaseDatabase.getInstance().getReference().child("user/" + friends.get(indexFriend).id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        HashMap mapFriend = (HashMap) dataSnapshot.getValue();
                        friends.get(indexFriend).name = (String) mapFriend.get("name");
                        friends.get(indexFriend).admin = (String) mapFriend.get("admin");
                        friends.get(indexFriend).email = (String) mapFriend.get("email");
                        friends.get(indexFriend).avatar = (String) mapFriend.get("avatar");
                    }
//          GroupDB.getInstance(context).addGroup(groups.get(indexGroup));
                    getUserInfo(indexFriend + 1);
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
}
