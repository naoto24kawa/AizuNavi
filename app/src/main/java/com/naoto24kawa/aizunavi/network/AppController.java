package com.naoto24kawa.aizunavi.network;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.Map;

public class AppController extends Application {

    private static final String TAG = AppController.class.getSimpleName();

    private static RequestQueue mRequestQueue;

    private static AppController mInstance;

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        mInstance = new AppController();
//        mInstance.getRequestQueue();
    }

//    public static AppController getInstance() {
//        if (mInstance == null) {
//            mInstance = new AppController();
//            mInstance.getRequestQueue();
//        }
//        return mInstance;
//    }
//
//    public Context getContext() {
//        if (mContext == null) {
//            mContext = mInstance.getApplicationContext();
//        }
//        return mContext;
//    }
//
//    public RequestQueue getRequestQueue() {
//        if (mRequestQueue == null) {
//            mRequestQueue = Volley.newRequestQueue(getContext());
//        }
//        return mRequestQueue;
//    }

    public static void http(Context context, int type, String url, final Map<String, String> parameters, Response.Listener<JSONObject> responseListener) {

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

        Volley.newRequestQueue(context).add(jsonObjReq);
    }
}
