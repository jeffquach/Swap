package com.example.jeff.swap;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

/**
 * Created by jeff on 15-01-11.
 */
public class JSONParser {
    private static InputStream inputStream = null;
    private static JSONObject jsonObject = null;
    private static JSONArray jsonArray = null;
    private static String json = "";

    public JSONParser(){}

    byte[] getUrlBytes(String urlSpec) throws IOException{
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try{
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                return null;
            }
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while((bytesRead = in.read(buffer)) > 0){
                out.write(buffer,0,bytesRead);
            }
            out.close();
            return out.toByteArray();
        }finally {
            connection.disconnect();
        }
    }

    public String getStringUsingHttp(String url){
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet getRequest = new HttpGet();
        try {
            getRequest.setURI(new URI(url));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        HttpResponse getResponse;
        try {
            getResponse = httpClient.execute(getRequest);
            HttpEntity httpEntity = getResponse.getEntity();
            inputStream = httpEntity.getContent();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,"iso-8859-1"),8);
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            while((line = reader.readLine()) != null){
                stringBuilder.append(line+"\n");
            }
            inputStream.close();
            json = stringBuilder.toString();
            if (json.equals("null\n")){
                return null;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }

    public JSONArray getJSONArray(String url){
        json = getStringUsingHttp(url);
        if (json == null){
            return null;
        }
        try{
            jsonArray = new JSONArray(json);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("JSON parse error","Error parsing JSON data: "+e.toString());
        }
        return jsonArray;
    }

    public JSONObject getJSONFromUrl(String url, List<NameValuePair> params){
        // Make HTTP request using the 'DefaultHttpClient'
        try{
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            if (params != null){
                httpPost.setEntity(new UrlEncodedFormEntity(params));
            }
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            inputStream = httpEntity.getContent();
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,"iso-8859-1"),8);
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            while((line = reader.readLine()) != null){
                stringBuilder.append(line+"\n");
            }
            inputStream.close();
            json = stringBuilder.toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch(Exception e){
            Log.e("Buffer error","Error converting result : "+e.toString());
        }
        try{
            jsonObject = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("JSON parser error","Error parsing data: "+e.toString());
        }
        return jsonObject;
    }
}
