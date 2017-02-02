package com.demo.cocoatest;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;


public class RestClient {

    private String url;
    private int code;
    private String message;
    private String response;
    HttpResponse httpResponse;
    private ArrayList<NameValuePair> params;
    private ArrayList<NameValuePair> headers;
    MultipartEntity paramsMultipart ;

    public RestClient(String url) {
        this.url = url;
        params = new ArrayList<>();
        headers = new ArrayList<>();
        paramsMultipart = new MultipartEntity();


    }

    public RestClient(String url, Context context) {
        this.url = url;
        params = new ArrayList<>();
        headers = new ArrayList<>();
        paramsMultipart = new MultipartEntity();

    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getResponse() {
        return response;
    }

    public HttpResponse getHttpResponse() {
        return httpResponse;
    }

    public void addHeader(String name, String value)
    {
        headers.add(new BasicNameValuePair(name, value));
    }

    public void addParam(String name, String value)
    {
        params.add(new BasicNameValuePair(name, value));
    }

    public void addMultiParamString(String name, String value)
    {
        try
        {
            paramsMultipart.addPart(name, new StringBody(value));
        }
        catch (Exception e){}
    }
    public void addMultiParamImage(String name, File value)
    {

        paramsMultipart.addPart(name, new FileBody(value));

    }

    public void executeGet() throws Exception
    {
        String combinedParams = "";
        if(params.isEmpty() == false)
        {
            combinedParams += "?";
            for(NameValuePair p : params)
            {
                String paramString = p.getName() + "=" + URLEncoder.encode(p.getValue(), "UTF-8");
                if(combinedParams.length() > 1)
                {
                    combinedParams  +=  "&" + paramString;
                }
                else
                {
                    combinedParams += paramString;
                }
            }
        }

        HttpGet request = new HttpGet(url + combinedParams);

        for(NameValuePair h : headers)
        {
            if(!request.containsHeader(h.getName())) {
                request.addHeader(h.getName(), h.getValue());
            }
        }
        executeRequest(request);
    }

    public void executePost() throws Exception
    {
        HttpPost request = new HttpPost(url);

        for(NameValuePair h : headers)
        {
            if(!request.containsHeader(h.getName())) {
                request.addHeader(h.getName(), h.getValue());
            }
        }

        if(params.isEmpty() == false)
        {
            request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
        }
        executeRequest(request);
    }

    public void executeDelete() throws Exception
    {
        String combinedParams = "";
        if(params.isEmpty() == false)
        {
            combinedParams += "?";
            for(NameValuePair p : params)
            {
                String paramString = p.getName() + "=" + URLEncoder.encode(p.getValue(), "UTF-8");
                if(combinedParams.length() > 1)
                {
                    combinedParams  +=  "&" + paramString;
                }
                else
                {
                    combinedParams += paramString;
                }
            }
        }

        HttpDelete request = new HttpDelete(url + combinedParams);
        for(NameValuePair h : headers)
        {
            if(!request.containsHeader(h.getName())) {
                request.addHeader(h.getName(), h.getValue());
            }
        }
        executeRequest(request);
    }

    public void executePut() throws Exception
    {
        String combinedParams = "";
        if(params.isEmpty() == false)
        {
            combinedParams += "?";
            for(NameValuePair p : params)
            {
                String paramString = p.getName() + "=" + URLEncoder.encode(p.getValue(), "UTF-8");
                if(combinedParams.length() > 1)
                {
                    combinedParams  +=  "&" + paramString;
                }
                else
                {
                    combinedParams += paramString;
                }
            }
        }

        HttpPut request = new HttpPut(url + combinedParams);
        for(NameValuePair h : headers)
        {
            if(!request.containsHeader(h.getName())) {
                request.addHeader(h.getName(), h.getValue());
            }
        }
        executeRequest(request);
    }
    public void executePostJSON(String json) throws Exception
    {
        HttpPost request = new HttpPost(url);
        request.setEntity(new StringEntity(json));
        request.setHeader("Content-Type", "application/json");
        for(NameValuePair h : headers)
        {
            if(!request.containsHeader(h.getName())) {
                request.addHeader(h.getName(), h.getValue());
            }
        }
        if(!params.isEmpty())
        {
            request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
        }
        executeRequest(request);

    }

    public void executePostMultipart() throws Exception
    {
        HttpPost request = new HttpPost(url);

        for(NameValuePair h : headers)
        {
            if(!request.containsHeader(h.getName())) {
                Log.d(h.getName(), h.getValue());
                request.addHeader(h.getName(), h.getValue());
            }
        }

        if(paramsMultipart != null)
        {
            request.setEntity(paramsMultipart);
        }
        executeMultipartRequest(request, url);
    }



    public void executePutMultipart() throws Exception
    {
        HttpPut request = new HttpPut(url);
        for(NameValuePair h : headers)
        {
            if(!request.containsHeader(h.getName())) {
                request.addHeader(h.getName(), h.getValue());
            }
        }
        request.setEntity(paramsMultipart);

        executeMultipartRequest(request, url);
    }

    public void executeRequest(HttpUriRequest request)
    {
        HttpClient client = new DefaultHttpClient();

        try
        {
            httpResponse = client.execute(request);
            code = httpResponse.getStatusLine().getStatusCode();
            message = httpResponse.getStatusLine().getReasonPhrase();

            HttpEntity entity = httpResponse.getEntity();

            if (entity != null)
            {
                InputStream inStream = entity.getContent();
                response = convertStreamToString(inStream);
                inStream.close();
            }

        }
        catch (Exception e)
        {
            client.getConnectionManager().shutdown();
        }
    }

    public String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        try
        {
            while ((line = reader.readLine()) != null)
            {
                sb.append(line + "\n");
            }
        }
        catch (IOException e)
        { }
        finally
        {
            try
            {
                is.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    private void executeMultipartRequest(HttpUriRequest request, String url)
    {
        HttpClient client = new DefaultHttpClient();
        HttpResponse httpResponse;
        try
        {
            httpResponse = client.execute(request);
            code = httpResponse.getStatusLine().getStatusCode();
            message = httpResponse.getStatusLine().getReasonPhrase();

            HttpEntity entity = httpResponse.getEntity();

            if (entity != null) {

                InputStream inStream = entity.getContent();
                response = convertStreamToString(inStream);
                inStream.close();
            }

        } catch (ClientProtocolException e)  {
            client.getConnectionManager().shutdown();
            e.printStackTrace();
        } catch (IOException e) {
            client.getConnectionManager().shutdown();
            e.printStackTrace();
        }
    }

}
