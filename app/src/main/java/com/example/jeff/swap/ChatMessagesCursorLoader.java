package com.example.jeff.swap;

import android.content.Context;
import android.database.Cursor;

/**
 * Created by jeff on 15-01-29.
 */
public class ChatMessagesCursorLoader extends SQLiteCursorLoader {
    private String person_chatting_with;

    public ChatMessagesCursorLoader(Context c, String person) {
        super(c);
        person_chatting_with = person;
    }

    @Override
    protected Cursor loadCursor() {
        return ChatManager.get(getContext()).queryChatMessages(person_chatting_with);
    }
}
