package com.example.jeff.swap.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jeff.swap.BuildConfig;
import com.example.jeff.swap.ChatDatabaseHelper;
import com.example.jeff.swap.ChatManager;
import com.example.jeff.swap.ChatMessagesCursorLoader;
import com.example.jeff.swap.JSONParser;
import com.example.jeff.swap.R;
import com.example.jeff.swap.SoftKeyboard;
import com.example.jeff.swap.activities.ChatActivity;
import com.example.jeff.swap.models.Chat;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by jeff on 15-01-25.
 */
public class ChatFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String PERSON_CHATTING_TO = "username";
    private static final String MESSAGE = "msg";
    private static final int LOAD_CHAT_MESSAGES = 0;
    private static final String PHONE_NUMBER_FROM_INTENT = "phoneNumberFromIntent";
    private static final int TYPING_TIMER_LENGTH = 113;

    private ChatDatabaseHelper.ChatCursor mChatCursor;
    private ChatManager mChatManager;

    private SharedPreferences prefs;
    private List<NameValuePair> params;
    private EditText chat_msg;
    private Button send_btn;
    private Bundle bundle;
    private TableLayout tableLayout;
    private SoftKeyboard softKeyboard;
    private InputMethodManager im;
    private LinearLayout mainLayout;
    private TextView chatTitle;
    private ScrollView scrollView;
    private SharedPreferences.Editor edit;

    private boolean shouldDisconnect = true;
    private Activity currentActivity;
    private String chatRoomName;
    private String currentUser;
    private static String currentlyChattingWith;
    private Socket mSocket;
    private boolean mTyping = false;
    private Handler mTypingHandler = new Handler();
    private TableRow liveTypingTableRow;
    private TextView liveTypingEditText;

    private LoaderManager loaderManager;
    private NotificationManager notificationManager;
    private String phoneNumberReceivedFromIntent;

    public static ChatFragment newInstance(String username){
        Bundle args = new Bundle();
        args.putString(PERSON_CHATTING_TO,username);
        ChatFragment chatFragment = new ChatFragment();
        chatFragment.setArguments(args);
        return chatFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        // Create menu
        setHasOptionsMenu(true);

        // Connect to websocket server
        {
            try {
                // DEV --> "http://10.0.2.2:3000" (HTTP! NOT HTTPS!)
                mSocket = IO.socket(BuildConfig.SERVER_URL);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        currentActivity = (ChatActivity) getActivity();
        prefs = getActivity().getSharedPreferences("Chat", 0);
        currentUser = prefs.getString("USERNAME","");

        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("typing", onTyping);
        mSocket.connect();

        Log.i("savedInstanceState","$$$ savedInstanceState $$$: "+(savedInstanceState==null));
        mChatManager = ChatManager.get(getActivity());
        Bundle args = getArguments();
        bundle = getActivity().getIntent().getBundleExtra("INFO");

        if (bundle != null){
            currentlyChattingWith = bundle.getString("username");

            int currentUserIntegerValue = getIntegerValueOfUser(currentUser);
            int currentChattingWithIntegerValue = getIntegerValueOfUser(currentlyChattingWith);

            if (currentChattingWithIntegerValue > currentUserIntegerValue){
                chatRoomName = (currentUser+"-"+currentlyChattingWith);
            }else{
                chatRoomName = (currentlyChattingWith+"-"+currentUser);
            }

            loaderManager = getLoaderManager();
            if (currentlyChattingWith != null && savedInstanceState == null){
                mSocket.emit("add user", currentlyChattingWith, chatRoomName, prefs.getString("currentChatRoomName",""));
                loaderManager.initLoader(LOAD_CHAT_MESSAGES,args,this);
            }else if(currentlyChattingWith != null && savedInstanceState != null){
                mSocket.emit("add user", currentlyChattingWith, chatRoomName, prefs.getString("currentChatRoomName",""));
                restartLoader();
            }
        }
    }

    public int getIntegerValueOfUser(String username){
        int result = 0;
        for(int i = 0; i < username.length(); i++){
            result += (int)username.charAt(i);
        }
        return result;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.activity_chat, container, false);
        Log.i("CHAT ACTIVITY 1","$$$ onCreate() called! $$$: "+currentlyChattingWith);
        tableLayout = (TableLayout) view.findViewById(R.id.tableLayout);
        edit = prefs.edit();
        edit.putString("CURRENTLY_ACTIVE",bundle.getString("phoneNumber")).commit();
        chatTitle = (TextView) view.findViewById(R.id.chatTitle);
        if (currentlyChattingWith != null){
            chatTitle.setText("Chatting with: "+currentlyChattingWith+" --> "+(bundle.getString("phoneNumber")));
            chatTitle.setTextSize(30);
        }
        chat_msg = (EditText) view.findViewById(R.id.chat_msg);
        im = (InputMethodManager) getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
        mainLayout = (LinearLayout) view.findViewById(R.id.chatLayout);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(onNotice, new IntentFilter("Msg"));

        // Find scrollview and add a listener for when the height changes mainly when the keyboard shows
        scrollView = (ScrollView) view.findViewById(R.id.scrollView);
        scrollView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (!isTablet(getActivity())){
                    chat_msg.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View v, boolean hasFocus) {
                            if (hasFocus) {
                                getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                            }
                        }
                    });
                }
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });

        // Find the 'send' button and create listener when a message is sent to place the message onto the table layout within the scrollview
        send_btn = (Button) view.findViewById(R.id.sendbtn);
        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String chatMessage = chat_msg.getText().toString();
                edit.putString(MESSAGE, chatMessage).commit();
                addChatMessageToTextView(getActivity(),"You",chatMessage,"#A901DB");
                mChatManager.insertChatMessage(String.format("%tB %<te, %<tY, %<tr", new Date()), currentlyChattingWith, "You", chatMessage);
                chat_msg.setText("");
                new Send().execute();
            }
        });

        // Add in previous chat room name
        edit.putString("currentChatRoomName",chatRoomName).commit();

        // For the 'EditText' of the input field where you enter the chat message
        chat_msg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (null == currentlyChattingWith) return;
                if (!mSocket.connected()) return;
                Log.i("onTextChanged,mTyping","$$$ mTyping is $$$: "+mTyping);
                if (!mTyping) {
                    mTyping = true;
                    String inputMessage = chat_msg.getText().toString().trim();
                    mSocket.emit("typing",inputMessage, chatRoomName, currentlyChattingWith, currentUser);
                }

                mTypingHandler.removeCallbacks(onTypingTimeout);
                mTypingHandler.postDelayed(onTypingTimeout, TYPING_TIMER_LENGTH);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        softKeyboard = new SoftKeyboard(mainLayout, im);

        registerSoftKeyboardCallbacks();
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu,inflater);
        inflater.inflate(R.menu.main_options_menu,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                if (NavUtils.getParentActivityName(getActivity()) != null){
                    NavUtils.navigateUpFromSameTask(getActivity());
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private Runnable onTypingTimeout = new Runnable() {
        @Override
        public void run() {
            if (!mTyping) return;
            mTyping = false;
        }
    };

    public void addChatMessageToTextView(Context context, String sender, String message, String textColor){
        TableRow tr1 = new TableRow(context);
        tr1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        TextView textView = new TextView(context);
        textView.setTextSize(20);
        textView.setTextColor(Color.parseColor(textColor));
        textView.setText(Html.fromHtml("<b>" + sender + " : </b>" + message));
        tr1.addView(textView);
        tableLayout.addView(tr1);
    }
    public void restartLoader(){
        loaderManager.restartLoader(LOAD_CHAT_MESSAGES,bundle,this);
    }


    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    @Override
    public void onStart(){
        super.onStart();
        edit.putString("CURRENTLY_ACTIVE",bundle.getString("phoneNumber")).commit();
        Log.i("CHAT ACTIVITY","$$$ onStart() called! $$$: "+currentlyChattingWith);
    }

    @Override
    public void onResume(){
        super.onResume();
        softKeyboard.setScreenOnState(true);
        Log.i("onResume","$$$ prefs.getString(\"CURRENTLY_ACTIVE\",\"\") $$$: "+(prefs.getString("CURRENTLY_ACTIVE","")));
        Log.i("onResume","$$$ prefs.getString(\"PHONE_NUMBER_FROM_INTENT\",\"\") $$$: "+(prefs.getString(PHONE_NUMBER_FROM_INTENT,"")));
        if (prefs.getString("CURRENTLY_ACTIVE","").equals(prefs.getString(PHONE_NUMBER_FROM_INTENT,""))){
            notificationManager = (NotificationManager) getActivity().getSystemService(getActivity().NOTIFICATION_SERVICE);
            notificationManager.cancel("chatMessageNotification",0);
        }
        initializeLiveTypingTableRow(getActivity());
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        softKeyboard.unRegisterSoftKeyboardCallback();
        mChatCursor = null;
        Log.i("CHAT ACTIVITY","$$$ onDestroy() called! currentlyChattingWith $$$: "+currentlyChattingWith);

        shouldDisconnect = prefs.getBoolean("doNotDisconnectFromSocketServer",true);
        Log.i("onDestroy","$$$ shouldDisconnect $$$: "+shouldDisconnect);
        if (shouldDisconnect){
            Log.i("onDestroy-->shouldDisconnect","$$$ socket DISCONNECTION SON! $$$");
            mSocket.disconnect();
            mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
            mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
            mSocket.off("typing", onTyping);
        }
        edit.putBoolean("doNotDisconnectFromSocketServer",true).commit();
    }

    @Override
    public void onPause(){
        super.onPause();
        softKeyboard.closeSoftKeyboard();
        softKeyboard.setScreenOnState(false);
        sendNotificationWhenNotChatting();
        removeTyping();
        if (chat_msg.getText().length() > 0){
            mSocket.emit("typing","", chatRoomName, currentlyChattingWith, currentUser);
        }
        //initializeLiveTypingTableRow();
        Log.i("CHAT ACTIVITY","$$$ onPause() called! $$$: "+currentlyChattingWith);
    }

    @Override
    public void onStop(){
        super.onStop();
        Log.i("CHAT ACTIVITY","$$$ onStop() called! $$$: "+currentlyChattingWith);
    }

    public void sendNotificationWhenNotChatting(){
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("CURRENTLY_ACTIVE",null);
        edit.commit();
    }

    public void registerSoftKeyboardCallbacks(){
        softKeyboard.setSoftKeyboardCallback(new SoftKeyboard.SoftKeyboardChanged()
        {

            @Override
            public void onSoftKeyboardHide(){
                Log.i("", "$$$ KEYBOARD HIDDEN! $$$$");
            }

            @Override
            public void onSoftKeyboardShow(){
                Log.i("", "$$$ KEYBOARD SHOWN! $$$$");
            }
        });
    }

    private BroadcastReceiver onNotice = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            removeTyping();
            initializeLiveTypingTableRow(context);
            String chatMessage = intent.getStringExtra("msg");
            String messageSender = intent.getStringExtra("username");
            String string2 = intent.getStringExtra("phoneNumber");
            phoneNumberReceivedFromIntent = bundle.getString("phoneNumber");
            if (string2.equals(phoneNumberReceivedFromIntent)){
                addChatMessageToTextView(context,messageSender,chatMessage,"#0B0719");
            }
        }
    };

    private void updateUI(){
        if (mChatCursor == null){
            return;
        }
        mChatCursor.moveToFirst();
        while(!mChatCursor.isAfterLast()){
            Chat chat = mChatCursor.getChat();
            TableRow tr1 = new TableRow(getActivity().getApplicationContext());
            TextView textView = new TextView(getActivity().getApplicationContext());
            textView.setTextSize(20);
            String messageSender = (String) chat.getMessageSender();
            if (messageSender.equals("You")){
                textView.setTextColor(Color.parseColor("#A901DB"));
            }else{
                textView.setTextColor(Color.parseColor("#0B0719"));
            }
            textView.setText(Html.fromHtml("<b>" + messageSender + " : </b>" + chat.getMessageBody()));
            tr1.addView(textView);
            tableLayout.addView(tr1);
            mChatCursor.moveToNext();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new ChatMessagesCursorLoader(getActivity(),currentlyChattingWith);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mChatCursor = (ChatDatabaseHelper.ChatCursor) cursor;
        updateUI();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (mChatCursor != null){
            mChatCursor.close();
        }
        mChatCursor = null;
    }

    private class Send extends AsyncTask<String,String, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... args) {
            JSONParser jsonParser = new JSONParser();
            params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("phoneNumberOfSender",prefs.getString("REGISTRATION_PHONE_NUMBER","")));
            params.add(new BasicNameValuePair("usernameOfSender",prefs.getString("USERNAME","")));
            params.add(new BasicNameValuePair("phoneNumberOfRecipient", bundle.getString("phoneNumber")));
            params.add(new BasicNameValuePair("messageBody", prefs.getString(MESSAGE,"")));
            JSONObject jsonObject = jsonParser.getJSONFromUrl(BuildConfig.SERVER_URL+"/chat/send",params);
            return jsonObject;
        }
        @Override
        protected void onPostExecute(JSONObject json){
            String response = null;
            try{
                response = json.getString("response");
                if (response.equals("Failure --> user does not exist")){
                    Toast.makeText(getActivity().getApplicationContext(), "The user has logged out you can't send any messages anymore", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("databaseState","Already loaded");
    }

    public void initializeLiveTypingTableRow(Context context){
        liveTypingTableRow = new TableRow(context);
        liveTypingEditText = new TextView(context);
    }

    private void addTyping(String messageSender, String message) {
        if (liveTypingEditText.getText().equals("")){
            liveTypingTableRow.addView(liveTypingEditText);
            tableLayout.addView(liveTypingTableRow);
        }
        liveTypingEditText.setTextSize(20);
        liveTypingEditText.setTextColor(Color.parseColor("#0B0719"));
        String typingMessage = messageSender + " is typing .....\n";
        String typingMessageAndText = (typingMessage + message);
        liveTypingEditText.setText(typingMessageAndText);
    }

    private void removeTyping() {
        tableLayout.removeView(liveTypingTableRow);
    }

    // Websocket callbacks passed to "emit()" function defined in 'onCreate'

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity().getApplicationContext(),
                            R.string.error_connect, Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            currentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String message,userChattingWith,senderOfMessage;
                    try {
                        message = data.getString("message");
                        userChattingWith = data.getString("currentlyChattingWith");
                        senderOfMessage = data.getString("senderOfMessage");
                    } catch (JSONException e) {
                        return;
                    }
                    int messageLength = message.length();

                    // ELSE IF condition called for when a user deletes their message and the length of message is 0 and the livetyping message gets removed from the other user's screen --> need to fix this when the user pauses and doesn't enter in anything or when they hold the delete button
                    if (messageLength > 0 && senderOfMessage.equals(currentlyChattingWith)){
                        addTyping(currentlyChattingWith,message);
                    }else if (messageLength == 0){
                        removeTyping();
                        initializeLiveTypingTableRow(currentActivity);
                    }
                }
            });
        }
    };
}