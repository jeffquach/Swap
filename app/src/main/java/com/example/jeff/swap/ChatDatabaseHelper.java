package com.example.jeff.swap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.jeff.swap.models.Chat;

/**
 * Created by jeff on 15-01-27.
 */
public class ChatDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "chat.sqlite";
    private static final int VERSION = 1;

    private static final String TABLE_CHAT = "chats";
    private static final String COLUMN_CHAT_ID = "_id";
    private static final String COLUMN_CHAT_DATE_MESSAGE_SENT = "date_message_sent";
    private static final String COLUMN_CHAT_PERSON_CHATTING_WITH = "person_chatting_with";
    private static final String COLUMN_CHAT_MESSAGE_SENDER = "message_sender";
    private static final String COLUMN_CHAT_MESSAGE_BODY = "message_body";

    public ChatDatabaseHelper(Context context){
        super(context,DB_NAME,null,VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table chats (_id integer primary key autoincrement, timestamp integer, date_message_sent varchar(50), person_chatting_with varchar(50), message_sender varchar(50), message_body text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public long insertChatMessage(String date, String person_chatting_with, String message_sender, String message_body){
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_CHAT_DATE_MESSAGE_SENT,date);
        contentValues.put(COLUMN_CHAT_PERSON_CHATTING_WITH,person_chatting_with);
        contentValues.put(COLUMN_CHAT_MESSAGE_SENDER, message_sender);
        contentValues.put(COLUMN_CHAT_MESSAGE_BODY, message_body);
        return getWritableDatabase().insert(TABLE_CHAT,null,contentValues);
    }

    public ChatCursor queryChatMessages(String person_chatting_with){
        Cursor cursor = getReadableDatabase().rawQuery("select * from chats where person_chatting_with = ? order by timestamp desc",new String[] {person_chatting_with});
        Log.i("cursor","$$$ cursor.getCount(): $$$ "+(cursor.getCount()));
        if (cursor.getCount() == 0){
            return null;
        }
        return new ChatCursor(cursor);
    }

    public void deleteChatMessage(String message){
        getWritableDatabase().execSQL("delete from chats where message_body = ?", new String[] {message});
    }

    public static class ChatCursor extends CursorWrapper{
        public ChatCursor(Cursor cursor){
            super(cursor);
        }
        public Chat getChat(){
            if (isBeforeFirst() || isAfterLast()){
                return null;
            }
            Chat chat = new Chat();
            chat.setDateMessageSent(getString(getColumnIndex(COLUMN_CHAT_DATE_MESSAGE_SENT)));
            chat.setMessageBody(getString(getColumnIndex(COLUMN_CHAT_MESSAGE_BODY)));
            chat.setMessageSender(getString(getColumnIndex(COLUMN_CHAT_MESSAGE_SENDER)));
            chat.setPersonChattingWith(getString(getColumnIndex(COLUMN_CHAT_PERSON_CHATTING_WITH)));
            return chat;
        }
    }

}
