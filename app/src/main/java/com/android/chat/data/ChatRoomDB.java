package com.android.chat.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.android.chat.model.ChatRoom;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vijay on 9/1/18.
 */

public class ChatRoomDB {
    private static ChatRoomDB.ChatRoomDBHelper mDbHelper = null;

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private ChatRoomDB() {
    }

    private static ChatRoomDB instance = null;

    public static ChatRoomDB getInstance(Context context) {
        if (instance == null) {
            instance = new ChatRoomDB();
            mDbHelper = new ChatRoomDB.ChatRoomDBHelper(context);
        }
        return instance;
    }

    public List<ChatRoom> getChatRooms() {
        List<ChatRoom> chatRooms = new ArrayList<>();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(ChatRoomDB.FeedEntry.QUERY_SELECT_ALL, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                ChatRoom chatRoom;
                while (cursor.moveToNext()) {
                    chatRoom = new ChatRoom();
                    chatRoom.id = cursor.getString(0);
                    chatRoom.roomId = cursor.getString(1);
                    chatRoom.name = cursor.getString(2);
                    chatRoom.email = cursor.getString(3);
                    chatRoom.avatar = cursor.getString(4);
                    chatRoom.isGroup = cursor.getInt(4) == 1;
                    chatRoom.status = cursor.getString(4);
                    chatRoom.timestamp = cursor.getLong(4);
                    chatRooms.add(chatRoom);
                }
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
        return chatRooms;
    }

    public void addChatRooms(List<ChatRoom> chatRooms) {
        for (ChatRoom chatRoom : chatRooms) {
            addChatRoom(chatRoom);
        }
    }

    public long addChatRoom(ChatRoom chatRoom) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ChatRoomDB.FeedEntry.COLUMN_NAME_ID, chatRoom.id);
        values.put(ChatRoomDB.FeedEntry.COLUMN_NAME_ROOM_ID, chatRoom.roomId);
        values.put(ChatRoomDB.FeedEntry.COLUMN_NAME_NAME, chatRoom.name);
        values.put(ChatRoomDB.FeedEntry.COLUMN_NAME_EMAIL, chatRoom.email);
        values.put(ChatRoomDB.FeedEntry.COLUMN_NAME_AVATAR, chatRoom.avatar);
        values.put(FeedEntry.COLUMN_NAME_IS_GROUP, chatRoom.isGroup ? 1 : 0);
        values.put(FeedEntry.COLUMN_NAME_STATUS, chatRoom.status);
        values.put(FeedEntry.COLUMN_NAME_TIME_STAMP, chatRoom.timestamp);
        return db.insert(ChatRoomDB.FeedEntry.TABLE_NAME, null, values);
    }

    public int deleteAll() {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        return db.delete(FeedEntry.TABLE_NAME, null, null);
    }

    public static class FeedEntry implements BaseColumns {
        static final String TABLE_NAME = "chatRoom";
        static final String COLUMN_NAME_ID = "memberId";
        static final String COLUMN_NAME_ROOM_ID = "roomId";
        static final String COLUMN_NAME_NAME = "name";
        static final String COLUMN_NAME_EMAIL = "email";
        static final String COLUMN_NAME_AVATAR = "avatar";
        static final String COLUMN_NAME_IS_GROUP = "isGroup";
        static final String COLUMN_NAME_STATUS = "status";
        static final String COLUMN_NAME_TIME_STAMP = "timestamp";


        static final String QUERY_SELECT_ALL = "select * from " + TABLE_NAME;
    }

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ChatRoomDB.FeedEntry.TABLE_NAME + " (" +
                    ChatRoomDB.FeedEntry.COLUMN_NAME_ID + TEXT_TYPE + COMMA_SEP +
                    ChatRoomDB.FeedEntry.COLUMN_NAME_ROOM_ID + TEXT_TYPE + COMMA_SEP +
                    ChatRoomDB.FeedEntry.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
                    ChatRoomDB.FeedEntry.COLUMN_NAME_EMAIL + TEXT_TYPE + COMMA_SEP +
                    ChatRoomDB.FeedEntry.COLUMN_NAME_AVATAR + TEXT_TYPE + COMMA_SEP +
                    ChatRoomDB.FeedEntry.COLUMN_NAME_IS_GROUP + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_STATUS + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_TIME_STAMP + TEXT_TYPE + COMMA_SEP +
                    "PRIMARY KEY (" + ChatRoomDB.FeedEntry.COLUMN_NAME_ID + COMMA_SEP +
                    ChatRoomDB.FeedEntry.COLUMN_NAME_ROOM_ID + ") )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ChatRoomDB.FeedEntry.TABLE_NAME;


    private static class ChatRoomDBHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        static final int DATABASE_VERSION = 1;
        static final String DATABASE_NAME = "chatRoomChat.db";

        ChatRoomDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }

        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }
}
