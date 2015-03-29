package com.example.jeff.swap;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.jeff.swap.models.Chat;

/**
 * Created by jeff on 15-01-27.
 */
public class ChatManager {

    private static final String CHAT_PREFERENCES = "conversations";
    private static final String SHARED_PREFERENCE_CURRENT_CHAT_ID = "ChatManager.currentChatId";

    private Context mAppContext;
    private ChatDatabaseHelper mHelper;
    private static ChatManager sChatManager;
    private static long mCurrentChatId;
    private SharedPreferences mSharedPreferences;

    public ChatManager(Context appContext){
        mAppContext = appContext;
        mHelper = new ChatDatabaseHelper(mAppContext);
        mSharedPreferences = mAppContext.getSharedPreferences(CHAT_PREFERENCES,Context.MODE_PRIVATE);
        mCurrentChatId = mSharedPreferences.getLong(SHARED_PREFERENCE_CURRENT_CHAT_ID,-1);
    }

    public static ChatManager get(Context context){
        if(sChatManager == null){
            sChatManager = new ChatManager(context.getApplicationContext());
        }
        return sChatManager;
    }

    public static boolean isTrackingChat(Chat chat){
        return chat != null && chat.getId() == mCurrentChatId;
    }

//    public Chat startNewChat(){
//        Chat chat = insertChat();
//        startTrackingChat(chat);
//        return chat;
//    }

    public ChatDatabaseHelper.ChatCursor queryChatMessages(String person_chatting_with){
        return mHelper.queryChatMessages(person_chatting_with);
    }

    public void startTrackingChat(Chat chat){
        mCurrentChatId = chat.getId();
        mSharedPreferences.edit().putLong(SHARED_PREFERENCE_CURRENT_CHAT_ID, mCurrentChatId).commit();
    }

    public void stopChat(){
        mCurrentChatId = -1;
        mSharedPreferences.edit().remove(SHARED_PREFERENCE_CURRENT_CHAT_ID).commit();
    }

    public void insertChatMessage(String date, String person_chatting_with, String message_sender, String message_body){
        mHelper.insertChatMessage(date,person_chatting_with,message_sender,message_body);
    }

    public void deleteChatMessage(String message_body){
        mHelper.deleteChatMessage(message_body);
    }

//    private Chat insertChat(){
//        Chat chat = new Chat();
//        chat.setId(mHelper.insertChat(chat));
//        return chat;
//    }
}
