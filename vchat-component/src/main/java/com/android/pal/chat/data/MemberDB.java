package com.android.pal.chat.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.android.pal.chat.model.Member;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admirar on 1/7/18.
 */

public class MemberDB {
    private static MemberDBHelper mDbHelper = null;

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private MemberDB() {
    }

    private static MemberDB instance = null;

    public static MemberDB getInstance(Context context) {
        if (instance == null) {
            instance = new MemberDB();
            mDbHelper = new MemberDB.MemberDBHelper(context);
        }
        return instance;
    }

    public List<Member> getMembers(String groupId) {
        List<Member> members = new ArrayList<>();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(FeedEntry.QUERY_SELECT_ALL_BY_GROUP, new String[]{groupId});
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                Member member;
                while (cursor.moveToNext()) {
                    member = new Member();
                    member.id = cursor.getString(0);
                    member.groupId = cursor.getString(1);
                    member.name = cursor.getString(2);
                    member.email = cursor.getString(3);
                    member.avatar = cursor.getString(4);
                    members.add(member);
                }
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
        return members;
    }

    public boolean isMemberExist(String memberId, String groupId) {
        boolean flag = false;
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(FeedEntry.QUERY_MEMBER_BY_ID_GROUP_ID, new String[]{memberId, groupId});
        if (cursor != null) {
            flag = cursor.getCount() > 0;
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
        return flag;
    }

    public void addMembers(List<Member> members, String groupId) {
        for (Member member : members) {
            addMember(member, groupId);
        }
    }

    public long addMember(Member member, String groupId) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(MemberDB.FeedEntry.COLUMN_NAME_ID, member.id);
        values.put(MemberDB.FeedEntry.COLUMN_NAME_GROUP_ID, groupId);
        values.put(MemberDB.FeedEntry.COLUMN_NAME_NAME, member.name);
        values.put(MemberDB.FeedEntry.COLUMN_NAME_EMAIL, member.email);
        values.put(MemberDB.FeedEntry.COLUMN_NAME_AVATA, member.avatar);
        if (isMemberExist(member.id, groupId)) {
            return db.update(FeedEntry.TABLE_NAME, values, FeedEntry.COLUMN_NAME_ID + "=? AND "
                    + FeedEntry.COLUMN_NAME_GROUP_ID + "=?", new String[]{member.id, groupId});
        } else {
            // Insert the new row, returning the primary key value of the new row
            return db.insert(MemberDB.FeedEntry.TABLE_NAME, null, values);
        }
    }

    public static class FeedEntry implements BaseColumns {
        static final String TABLE_NAME = "member";
        static final String COLUMN_NAME_ID = "memberId";
        static final String COLUMN_NAME_GROUP_ID = "groupId";
        static final String COLUMN_NAME_NAME = "name";
        static final String COLUMN_NAME_EMAIL = "email";
        static final String COLUMN_NAME_AVATA = "avatar";

        static final String QUERY_SELECT_ALL_BY_GROUP = "select * from " + TABLE_NAME
                + " where " + COLUMN_NAME_GROUP_ID + "=?";
        static final String QUERY_MEMBER_BY_ID_GROUP_ID = "select * from " + TABLE_NAME
                + " where " + COLUMN_NAME_ID + "=? AND " + COLUMN_NAME_GROUP_ID + "=?";
    }

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
                    FeedEntry.COLUMN_NAME_ID + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_GROUP_ID + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_EMAIL + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_AVATA + TEXT_TYPE + COMMA_SEP +
                    "PRIMARY KEY (" + FeedEntry.COLUMN_NAME_ID + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_GROUP_ID + ") )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + MemberDB.FeedEntry.TABLE_NAME;


    private static class MemberDBHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        static final int DATABASE_VERSION = 1;
        static final String DATABASE_NAME = "MemberChat.db";

        MemberDBHelper(Context context) {
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
