package com.example.jeff.swap;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.jeff.swap.fragments.LoginFragment;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultRequestDirector;
import org.apache.http.protocol.HttpContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.tester.org.apache.http.FakeHttpLayer;
import org.robolectric.tester.org.apache.http.TestHttpResponse;
import org.robolectric.util.Strings;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.robolectric.util.FragmentTestUtil.startFragment;

/**
 * Created by jeff on 15-02-19.
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk=18)
public class MainActivityRobolectric {

    private LoginFragment loginFragment;
    private Button loginButton;
    private EditText username, phoneNumber;
    private ConnectionKeepAliveStrategy connectionKeepAliveStrategy;
    private DefaultRequestDirector defaultRequestDirector;

    @Before
    public void setUp() throws Exception{
        loginFragment = new LoginFragment();
        startFragment(loginFragment);
        View loginView = loginFragment.getView();
        loginButton = (Button) loginView.findViewById(R.id.log_btn);
        username = (EditText) loginView.findViewById(R.id.username);
        phoneNumber = (EditText) loginView.findViewById(R.id.phoneNumber);

        FakeHttpLayer fakeHttpLayer = Robolectric.getFakeHttpLayer();
        assertFalse(fakeHttpLayer.hasPendingResponses());
        assertFalse(fakeHttpLayer.hasRequestInfos());
        assertFalse(fakeHttpLayer.hasResponseRules());
        assertNull(fakeHttpLayer.getDefaultResponse());

        connectionKeepAliveStrategy = new ConnectionKeepAliveStrategy() {
            @Override
            public long getKeepAliveDuration(HttpResponse httpResponse, HttpContext httpContext) {
                return 0;
            }
        };

        defaultRequestDirector = new DefaultRequestDirector(null,null,null,connectionKeepAliveStrategy,null,null,null,null,null,null,null,null);
    }

    @After
    public void tearDown() throws Exception{

    }

    @Test
    public void shouldHaveLoginFragment() throws Exception{
        assertNotNull(loginFragment);
    }

    @Test
    public void shouldHaveLoginButton() throws Exception{
        assertNotNull(loginButton);
        assertThat(loginButton.getText().toString(),equalTo("Login"));
    }

    @Test
    public void shouldBeAbleToLogin() throws IOException, HttpException {
        username.setText("jamareus");
        phoneNumber.setText("111");
        //loginButton.performClick();
        Robolectric.addPendingHttpResponse(new TestHttpResponse(200, "a happy response body"));
        HttpResponse response = defaultRequestDirector.execute(null, new HttpGet("http://example.com"), null);

        assertNotNull(response);
        assertThat(response.getStatusLine().getStatusCode(), equalTo(200));
        assertThat(Strings.fromStream(response.getEntity().getContent()),equalTo("a happy response body"));
    }
}
