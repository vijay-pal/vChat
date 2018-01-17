package com.android.pal.chat.data.firebase;

import com.android.pal.chat.model.ChatRoom;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by admirar on 1/10/18.
 */

public class SearchPeopleValueEvent implements ValueEventListener {
    private SearchPeopleListener mListener;
    private List<ChatRoom> chatRooms;
    private String searchText;

    public SearchPeopleValueEvent(SearchPeopleListener mListener, String searchText) {
        chatRooms = new ArrayList<>();
        this.mListener = mListener;
        this.searchText = searchText;
        FirebaseDatabase.getInstance().getReference().child("user").addListenerForSingleValueEvent(this);
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if (dataSnapshot.getValue() != null) {
            HashMap mapListUser = (HashMap) dataSnapshot.getValue();
            Iterator iterator = mapListUser.keySet().iterator();
            while (iterator.hasNext()) {
                final ChatRoom chatRoom = new ChatRoom();
                chatRoom.id = iterator.next().toString();
                HashMap user = (HashMap) mapListUser.get(chatRoom.id);
                chatRoom.name = (String) user.get("name");
                chatRoom.email = (String) user.get("email");
                chatRoom.avatar = (String) user.get("avatar");
                if (chatRoom.containsIgnoreCare(searchText)) {
                    chatRooms.add(chatRoom);
                    if (chatRooms.size() >= 5) {
                        break;
                    }
                }
            }
            if (mListener != null) {
                mListener.onSearchCompleted(chatRooms);
            }
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        if (mListener != null) {
            mListener.onSearchCancelled();
        }
    }

    public interface SearchPeopleListener {
        void onSearchCompleted(List<ChatRoom> chatRooms);

        void onSearchCancelled();
    }
}
