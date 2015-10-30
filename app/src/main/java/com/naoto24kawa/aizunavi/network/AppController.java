package com.naoto24kawa.aizunavi.network;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.Map;

public class AppController extends Application {

    private static final String TAG = AppController.class.getName();

    private static RequestQueue mRequestQueue;

    private static AppController mInstance;

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        mContext = getApplicationContext();
    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    public synchronized static RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getAppContext());
        }
        return mRequestQueue;
    }


    public <T> void addToRequestQueue(JsonObjectRequest req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public static Context getAppContext() {
        return mContext;
    }

    public static void http(int type, String url, final Map<String, String> parameters, Response.Listener<JSONObject> responseListener) {
        // responseListenerが登録されていない際のログ処理
        if (responseListener == null) {
            responseListener = new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d("VOLLEY.onResponse", response.toString());
                }
            };
        }

        JSONObject param = null;
        if (parameters != null) {
            param = new JSONObject(parameters);
        }

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(type, url, param, responseListener,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.d("VOLLEY", "Error: " + error.getMessage());
                    }
                });

        AppController.getInstance().addToRequestQueue(jsonObjReq);
    }
}
