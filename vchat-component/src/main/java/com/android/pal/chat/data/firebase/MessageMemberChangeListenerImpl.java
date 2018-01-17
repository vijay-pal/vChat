package com.android.pal.chat.data.firebase;

import android.content.Context;

import com.android.pal.chat.model.Member;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

/**
 * Created by admirar on 1/8/18.
 */

public class MessageMemberChangeListenerImpl implements ChildEventListener {

    private Context context;
    private String roomId;
    private List<Member> members;

    public MessageMemberChangeListenerImpl(Context context, String roomId, List<Member> members) {
        this.context = context;
        this.roomId = roomId;
        this.members = members;
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        Object userId = dataSnapshot.getValue();
        if (userId != null) {
            FirebaseDatabase.getInstance().getReference().child("user/" + dataSnapshot.getValue())
                    .addValueEventListener(
                            new GroupMemberValueEventListenerImpl(context, userId.toString(), roomId, members));
        }
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
