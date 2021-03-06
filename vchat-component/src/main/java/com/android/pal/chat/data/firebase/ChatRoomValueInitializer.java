package com.android.pal.chat.data.firebase;

import android.content.Context;

import com.android.pal.chat.base.StaticConfig;
import com.android.pal.chat.model.ChatRoom;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vijay on 9/1/18.
 */

public class ChatRoomValueInitializer implements GroupValueEventListenerImpl.GroupRefreshCompletedListener {
    private GroupValueEventListenerImpl.GroupRefreshCompletedListener mListener;
    private Context context;
    private List<ChatRoom> chatRooms;

    public ChatRoomValueInitializer(Context context, GroupValueEventListenerImpl.GroupRefreshCompletedListener mListener) {
        this.context = context;
        this.mListener = mListener;
        this.chatRooms = new ArrayList<>();

        FirebaseDatabase.getInstance().getReference().child("user/" + StaticConfig.UID + "/group")
                .addListenerForSingleValueEvent(new GroupValueEventListenerImpl(this, this.chatRooms, context));
    }

    private void getFriendChatRooms() {
        FirebaseDatabase.getInstance().getReference().child("friend/" + StaticConfig.UID)
                .addListenerForSingleValueEvent(new FriendValueEventListenerImpl(this, chatRooms, context));
    }

    @Override
    public void onCompleted(List<ChatRoom> groups) {
        if (mListener != null) {
            mListener.onCompleted(groups);
        }
    }

    @Override
    public void onCancelled() {
        if (mListener != null) {
            mListener.onCancelled();
        }
    }

    @Override
    public void fetchFriend() {
        getFriendChatRooms();
    }
}
