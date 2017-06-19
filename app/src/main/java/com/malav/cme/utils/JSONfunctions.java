package com.malav.cme.utils;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class JSONfunctions
{

	 private static InputStream is = null;
	 private static String result = "";
	 private static JSONObject jArray = null;
	 private static JSONArray obj = null;
	 
	public static JSONObject getJSONfromURL(String url) 
	{
		// Download JSON data from URL
		try 
		{
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(url);
			httppost.setHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/12.10240 ");
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();
		} 
		catch (Exception e) 
		{
			Log.e("log_tag", "Error in http connection " + e.toString());
		}

		try 
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) 
			{
				sb.append(line + "\n");
			}
			is.close();
			result = sb.toString();
		} 
		catch (Exception e) 
		{
			Log.e("log_tag", "Error converting result " + e.toString());
		}
		try 
		{
			jArray = new JSONObject(result);
		} 
		catch (JSONException e)
		{
			Log.e("log_tag", "Error parsing data " + e.toString());
		}
		return jArray;
	}
	
	public static JSONObject makeHttpRequest(String loginUrl, String post, List<NameValuePair> para)
    {
		try 
        {
            if(CommonUtils.equalIgnoreCase(post, "POST"))
            {
            	
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(loginUrl);
                
                httpPost.setEntity(new UrlEncodedFormEntity(para));
                httpPost.setHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/12.10240 ");
                HttpResponse httpResponse = httpClient.execute(httpPost);
                
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();
                
            }
            else if(CommonUtils.equalIgnoreCase(post, "GET"))
            {
            	HttpClient httpClient = new DefaultHttpClient();
                String paramString = URLEncodedUtils.format(para, "utf-8");
                loginUrl += "?" + paramString;
                HttpGet httpGet = new HttpGet(loginUrl);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();
            }
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try 
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            if (is != null) 
            {
                while ((line = reader.readLine()) != null) 
                {
                    sb.append(line + "\n");
                }
                is.close();
                result = sb.toString();
                Log.e("JSON Parser", "RESULT " + result);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        try
        {
            jArray = new JSONObject(result);
        }
        catch (JSONException e)
        {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
            e.printStackTrace();
        }
        return jArray;
    }
	
	public static JSONArray makeHttpRequestArray(String loginUrl, String post, List<NameValuePair> para)
    {
        try 
        {
            if(CommonUtils.equalIgnoreCase(post, "POST"))
            {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(loginUrl);
                httpPost.setEntity(new UrlEncodedFormEntity(para));
                httpPost.setHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/12.10240 ");
                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();
                
            }
            else if(CommonUtils.equalIgnoreCase(post, "GET"))
            {
                HttpClient httpClient = new DefaultHttpClient();
                String paramString = URLEncodedUtils.format(para, "utf-8");
                loginUrl += "?" + paramString;
                HttpGet httpGet = new HttpGet(loginUrl);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();
            }
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try 
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            if (is != null) 
            {
                while ((line = reader.readLine()) != null) 
                {
                    sb.append(line + "\n");
                }
                is.close();
                result = sb.toString();
            }
        }
        catch (Exception e)
        {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }
        try
        {
            obj = new JSONArray(result);
        }
        catch (JSONException e)
        {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }
        return obj;
    }
	
	
}
