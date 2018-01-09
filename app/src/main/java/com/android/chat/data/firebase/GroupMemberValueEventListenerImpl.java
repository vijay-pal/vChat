package com.android.chat.data.firebase;

import android.content.Context;

import com.android.chat.data.MemberDB;
import com.android.chat.model.Member;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

/**
 * Created by admirar on 1/7/18.
 */

public class GroupMemberValueEventListenerImpl implements ValueEventListener {

    private Context context;
    private String userId;
    private String roomId;
    private List<Member> members;

    public GroupMemberValueEventListenerImpl(Context context, String userId, String roomId, List<Member> members) {
        this.context = context;
        this.userId = userId;
        this.roomId = roomId;
        this.members = members;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if (dataSnapshot.getValue() != null) {
            HashMap hashMap = (HashMap) dataSnapshot.getValue();
            Member member = new Member();
            member.groupId = roomId;
            member.id = userId;
            member.name = (String) hashMap.get("name");
            member.email = (String) hashMap.get("email");
            member.avatar = (String) hashMap.get("avatar");
            members.add(member);
            MemberDB.getInstance(context).addMember(member, roomId);
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
