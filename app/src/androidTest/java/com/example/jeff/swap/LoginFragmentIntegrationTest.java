package com.example.jeff.swap;

import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.jeff.swap.activities.ChatActivity;
import com.example.jeff.swap.activities.MainActivity;
import com.example.jeff.swap.fragments.LoginFragment;
import com.github.nkzawa.socketio.client.Socket;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by jeff on 15-02-21.
 */
public class LoginFragmentIntegrationTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private MainActivity activity;
    private LoginFragment loginFragment;
    private EditText username, phoneNumber;
    private Button loginButton;
    private Socket mSocket;

    public LoginFragmentIntegrationTest(){
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception{
        super.setUp();
        setActivityInitialTouchMode(false);
        activity = getActivity();
    }

    @Override
    protected void tearDown() throws Exception{
        super.tearDown();
    }

    @LargeTest
    public void testLogin() throws Exception{
        // TESTS ARE RUN ON: "nexus-5-21-2, RUN IN DEBUG MODE (MAKE SURE TO CHECK 'BUILD VARIANTS' SECTION) AND START NODE SERVER LOCALLY
        // ** IF YOU DELETE THE APP FROM THE DEVICE YOU HAVE TO REMOVE THAT PARTICULAR USER FROM MONGO AND RE-REGISTER AND GET A NEW REGISTRATION ID IN THE DATABASE
        final ListView userListView = (ListView) activity.findViewById(R.id.listView);
        Button logoutButton = (Button) activity.findViewById(R.id.logout);
        Button refreshButton = (Button) activity.findViewById(R.id.refresh);
        assertNotNull(refreshButton);
        assertNotNull(logoutButton);
        assertNotNull(userListView);
        assertEquals(refreshButton.getText(), "Refresh");
        assertEquals(logoutButton.getText(),"Logout");
        final CountDownLatch signal = new CountDownLatch(1);
        signal.await(5, TimeUnit.SECONDS);
        Log.d("TINGZ","$$$ Items in listview $$$: " + (userListView.getCount()));
        assertEquals(userListView.getCount(), 5);
        Instrumentation.ActivityMonitor monitor = getInstrumentation().addMonitor(
                ChatActivity.class.getName(), null, false);

        for (int i = 0; i < userListView.getChildCount(); i++){
            TextView username = (TextView) userListView.getChildAt(i).findViewById(R.id.username);
            Log.d("TINGZ","$$$ username in listview $$$: " + username.getText());
            Log.d("TINGZ","$$$ username.getText().equals(\"merp\") $$$: " + username.getText().equals("merp"));
            if (username.getText().equals("merp")){
                TouchUtils.clickView(this,userListView.getChildAt(i));
                break;
            }
        }
        ChatActivity chatActivity = (ChatActivity) monitor.waitForActivityWithTimeout(2000);
        assertNotNull(chatActivity);
        LinearLayout chatLayout = (LinearLayout) chatActivity.findViewById(R.id.chatLayout);
        signal.await(5, TimeUnit.SECONDS);
        assertNotNull(chatLayout);

        // Send post request and receive message
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(BuildConfig.SERVER_URL+"/chat/send");
        JSONParser jsonParser = new JSONParser();
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("phoneNumberOfSender","11"));
        params.add(new BasicNameValuePair("usernameOfSender","merp"));
        params.add(new BasicNameValuePair("phoneNumberOfRecipient", "5212"));
        params.add(new BasicNameValuePair("messageBody", "Bing-bong yo!"));
        try {
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params);
            httpPost.setEntity(entity);
        } catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        try {
            HttpResponse response = httpClient.execute(httpPost);
            // write response to log
            Log.d("Http Post Response:", response.toString());
        } catch (ClientProtocolException e) {
            // Log exception
            e.printStackTrace();
        } catch (IOException e) {
            // Log exception
            e.printStackTrace();
        }
        signal.await(3, TimeUnit.SECONDS);

        // Find message in tablelayout
        TableLayout tableLayout = (TableLayout) chatActivity.findViewById(R.id.tableLayout);
        TableRow lastTableRow = (TableRow) tableLayout.getChildAt(tableLayout.getChildCount() - 1);
        Log.d("TABLE ROW","$$$ Children count! $$$: "+(lastTableRow.getVirtualChildCount()));
        TextView message = (TextView) lastTableRow.getVirtualChildAt(0);
        assertEquals(message.getText().toString(), "merp : Bing-bong yo!");

        ChatManager mChatManager = ChatManager.get(chatActivity);
        mChatManager.deleteChatMessage("Bing-bong yo!");

        // send message to other person
        final EditText sendMessage = (EditText) chatActivity.findViewById(R.id.chat_msg);
        chatActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sendMessage.setText("I like apples");
            }
        });

        // Test to see that live chatting doesn't appear in own chat window
        TableLayout tableLayout2 = (TableLayout) chatActivity.findViewById(R.id.tableLayout);
        TableRow lastTableRow2 = (TableRow) tableLayout2.getChildAt(tableLayout2.getChildCount() - 1);
        TextView messageSent = (TextView) lastTableRow2.getVirtualChildAt(0);
        assertNotSame(messageSent,"You is typing .....\nI like apples");

        Button sendButton = (Button) chatActivity.findViewById(R.id.sendbtn);
        TouchUtils.clickView(this,sendButton);
        signal.await(2, TimeUnit.SECONDS);
        lastTableRow2 = (TableRow) tableLayout2.getChildAt(tableLayout2.getChildCount() - 1);
        messageSent = (TextView) lastTableRow2.getVirtualChildAt(0);
        assertEquals(messageSent.getText().toString(), "You : I like apples");
        mChatManager.deleteChatMessage("I like apples");

        // Socket Test

        HttpGet getRequest = new HttpGet();
        getRequest.setURI(new URI("http://10.0.2.2:3000/test"));
        HttpResponse getResponse = httpClient.execute(getRequest);
        String html = EntityUtils.toString(getResponse.getEntity());
        Log.d("HTTP GET","$$$ Http GET Response: $$$ " + html);
        getResponse.getEntity().consumeContent();
        signal.await(3, TimeUnit.SECONDS);

        TableLayout tableLayoutSocket = (TableLayout) chatActivity.findViewById(R.id.tableLayout);
        TableRow lastTableRowSocket = (TableRow) tableLayoutSocket.getChildAt(tableLayoutSocket.getChildCount() - 1);
        TextView messageSocket = (TextView) lastTableRowSocket.getVirtualChildAt(0);
        Log.d("TABLE ROW","$$$ socket message $$$: "+(messageSocket.getText().toString()));
        assertNotNull(messageSocket.getText().toString());
        assertEquals(messageSocket.getText().toString(),"merp is typing .....\nTURD MEISTER HTML");

    }
}
