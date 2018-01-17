package com.android.pal.chat.data.firebase;

import android.util.Log;

import com.android.pal.chat.model.Message;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.HashMap;

/**
 * Created by admirar on 1/8/18.
 */

public class MessageChildChangeListenerImpl implements ChildEventListener {
    private MessageChangeListener messageListener;

    public MessageChildChangeListenerImpl(MessageChangeListener messageListener) {
        this.messageListener = messageListener;
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        if (dataSnapshot.getValue() != null) {
            Log.i("data", "rf::" + dataSnapshot.getRef());
            HashMap mapMessage = (HashMap) dataSnapshot.getValue();
            Message newMessage = new Message();
            newMessage.type = (String) mapMessage.get("type");
            newMessage.thumbnail = (String) mapMessage.get("thumbnail");
            newMessage.fileName = (String) mapMessage.get("fileName");
            newMessage.idSender = (String) mapMessage.get("idSender");
            newMessage.idReceiver = (String) mapMessage.get("idReceiver");
            newMessage.text = (String) mapMessage.get("text");
            newMessage.localUri = (String) mapMessage.get("localUri");
            newMessage.downloadUri = (String) mapMessage.get("downloadUri");
            newMessage.timestamp = (long) mapMessage.get("timestamp");
            if (messageListener != null) {
                messageListener.onChildAdded(newMessage);
            }
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

    public interface MessageChangeListener {
        void onChildAdded(Message message);
    }
}
