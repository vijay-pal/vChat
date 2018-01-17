package com.android.pal.chat.data.firebase;

import android.content.Context;

import com.android.pal.chat.data.GroupDB;
import com.android.pal.chat.model.ChatRoom;
import com.android.pal.chat.model.Group;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
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
        GroupDB.getInstance(context).dropDB();

        if (dataSnapshot.getValue() != null) {
            HashMap mapListGroup = (HashMap) dataSnapshot.getValue();
            Iterator iterator = mapListGroup.keySet().iterator();
            while (iterator.hasNext()) {
                String idGroup = (String) mapListGroup.get(iterator.next().toString());
                final ChatRoom newGroup = new ChatRoom();
                newGroup.id = idGroup;
                newGroup.isGroup = true;
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
                    Group group = new Group();
                    group.id = groups.get(indexGroup).id;
                    if (dataSnapshot.getValue() != null) {
                        HashMap mapGroup = (HashMap) dataSnapshot.getValue();
                        ArrayList<String> member = (ArrayList<String>) mapGroup.get("member");
                        HashMap mapGroupInfo = (HashMap) mapGroup.get("groupInfo");
                        for (String idMember : member) {
                            group.member.add(idMember);
                        }
                        String name = (String) mapGroupInfo.get("name");
                        group.groupInfo.put("name", name);
                        groups.get(indexGroup).name = name;

                        name = (String) mapGroupInfo.get("admin");
                        group.groupInfo.put("admin", name);
                        groups.get(indexGroup).admin = name;

                        name = (String) mapGroupInfo.get("avatar");
                        group.groupInfo.put("avatar", name);
                        groups.get(indexGroup).avatar = name;
                    }
                    GroupDB.getInstance(context).addGroup(group);
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
