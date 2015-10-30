package com.naoto24kawa.aizunavi.activities;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import com.naoto24kawa.aizunavi.R;
import com.naoto24kawa.aizunavi.network.ApiContents;
import com.naoto24kawa.aizunavi.network.AppController;
import org.json.JSONObject;
import com.android.volley.Response;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class MapActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        OnMapReadyCallback {

/***************************************************************************************************
**** Instances *************************************************************************************
***************************************************************************************************/

    // set TAG contents
    private static final String TAG = MapActivity.class.getSimpleName();

    // To need Initialize instance
    private GoogleApiClient mGoogleClient;
    private LocationRequest locationRequest;

    // instance of this activity
    private MapFragment mMapFragment;
    private GoogleMap mMap;
    private FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;
    private Location location;

/***************************************************************************************************
**** Activity Cycles *******************************************************************************
***************************************************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        // view initialize
        setContentView(R.layout.activity_map);
        mMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);
        mMapFragment.getMapAsync(this);
        mMap = mMapFragment.getMap();

        // instance initialize
        initialize();

        // Map Maker initialize
        getSpotMarkers();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        // locationServiceに接続する
        if (mGoogleClient != null) {
            mGoogleClient.connect();
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        // locationServiceに切断する
        if (mGoogleClient.isConnected()) {
            mGoogleClient.disconnect();
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        // locationServiceに切断する
        if (mGoogleClient.isConnected()) {
            mGoogleClient.disconnect();
        }
    }

/***************************************************************************************************
**** GoogleApiClients ******************************************************************************
***************************************************************************************************/

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");

        // 現在位置取得
        Location currentLocation = fusedLocationProviderApi.getLastLocation(mGoogleClient);
        if (currentLocation != null && currentLocation.getTime() > 20000) {
            location = currentLocation;
            // 現在位置表示
            animateCameraForTarget(mMap, location);
        } else {
            fusedLocationProviderApi.requestLocationUpdates(mGoogleClient, locationRequest, this);
            // 位置情報更新のリクエスト数を制限する
            Executors.newScheduledThreadPool(1).schedule(new Runnable() {
                @Override
                public void run() {
                    fusedLocationProviderApi.removeLocationUpdates(mGoogleClient, MapActivity.this);
                }
            }, 60000, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "onConnectionSuspended");
        // locationServiceの接続が中断された場合
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed");
        // locationServiceの接続に失敗した場合
    }

/***************************************************************************************************
**** Map Performances ******************************************************************************
***************************************************************************************************/

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");
        googleMap.setMyLocationEnabled(true);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged");
    }

    /**
     * 目的地にカメラを移動する
     * @param map マップ
     * @param target 位置
     */
    private void animateCameraForTarget(GoogleMap map, LatLng target) {
        map.animateCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(target)
                        .zoom(18.0f)
                        .build()));
    }

    /**
     * 目的地にカメラを移動する
     * @param map マップ
     * @param target 位置
     */
    private void animateCameraForTarget(GoogleMap map, Location target) {
        map.animateCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(new LatLng(target.getLatitude(), target.getLongitude()))
                        .zoom(18.0f)
                        .build()));
    }

    /**
     * HTTP通信を行い必要なマーカーを用意する
     */
    public void getSpotMarkers() {
        Log.d(TAG, "getSpotMarkers");
        // 会津バスバス停位置データ取得
        AppController.http(ApiContents.HTTP_GET, ApiContents.AIZUBUS_BUS_STOP_DATA, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // TODO:仮の処理です nisihikawa_naoto 2015/10/30
                        Log.d("VOLLEY.onResponse", response.toString());
                    }
                });
    }

/***************************************************************************************************
**** Another Methods *******************************************************************************
***************************************************************************************************/

    /**
     * 初期化処理
     */
    private void initialize() {
        Log.d(TAG, "initialize");
        // locationRequest
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(16);

        // GoogleApiClient
        mGoogleClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }
}
