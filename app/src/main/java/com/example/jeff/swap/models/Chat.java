package com.example.jeff.swap.models;

import java.util.Date;

/**
 * Created by jeff on 15-01-25.
 */
public class Chat {
    private long mId;
    private String mDateMessageSent;
    private String messageSender;
    private String messageBody;
    private String personChattingWith;

    public Chat(){
        mId = -1;
    }

    public long getId(){
        return mId;
    }

    public void setId(long id){
        mId = id;
    }

    public static String formatDuration(Date date){
        return String.format("%tD %<tr",date);
    }

    public String getMessageSender(){
        return messageSender;
    }

    public void setMessageSender(String sender){
        messageSender = sender;
    }

    public String getMessageBody(){
        return messageBody;
    }

    public void setMessageBody(String body){
        messageBody = body;
    }

    public String getDateMessageSent(){ return mDateMessageSent; }

    public void setDateMessageSent(String date){ mDateMessageSent = date; }

    public String getPersonChattingWith(){
        return personChattingWith;
    }

    public void setPersonChattingWith(String person){
        personChattingWith = person;
    }
}
