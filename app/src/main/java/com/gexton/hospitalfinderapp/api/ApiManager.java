package com.gexton.hospitalfinderapp.api;

import android.app.Activity;
import android.util.Log;

import com.gexton.hospitalfinderapp.models.HospitalBean;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;

public class ApiManager {

    Activity activity;
    String getOrPost;
    String apiName;
    RequestParams params;
    ApiCallback apiCallback;
    
    String baseURL = "https://maps.googleapis.com/";
    public static final String API_HOME_LIST = "maps/api/place/nearbysearch/json?";

    public ApiManager(Activity activity, String getOrPost, String apiName, RequestParams params, ApiCallback apiCallback) {
        this.activity = activity;
        this.getOrPost = getOrPost;
        this.apiName = apiName;
        this.params = params;
        this.apiCallback = apiCallback;

        System.out.println("-- Req URL : " + baseURL + apiName);
        System.out.println("-- Params : " + params.toString());
    }

    public void loadURL() {

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(baseURL + apiName, params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        try {
                            String content = new String(responseBody);
                            apiCallback.onApiResponce(statusCode, 1, apiName, content);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        try {
                            String content = new String(responseBody);
                            apiCallback.onApiResponce(statusCode, 0, apiName, content);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
    }

}