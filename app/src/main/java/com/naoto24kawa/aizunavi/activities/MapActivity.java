package com.naoto24kawa.aizunavi.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.naoto24kawa.aizunavi.R;
import com.naoto24kawa.aizunavi.entities.BusStop;
import com.naoto24kawa.aizunavi.entities.HistricSpot;
import com.naoto24kawa.aizunavi.entities.Spot;
import com.naoto24kawa.aizunavi.network.ApiContents;
import com.naoto24kawa.aizunavi.network.AppController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.android.volley.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class MapActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        OnMapReadyCallback {

/***************************************************************************************************
**** Instances *************************************************************************************
***************************************************************************************************/

    // set TAG contents
    private static final String TAG = MapActivity.class.getSimpleName();
    private static final int REQUEST_LOCATION_SET = 0;


    // To need Initialize instance
    private GoogleApiClient mGoogleClient;
    private LocationRequest locationRequest;

    // instance of this activity
    private MapFragment mMapFragment;
    private GoogleMap mMap;
    private FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;
    private Location location;
    private List<BusStop> busStopList;
    private List<Spot> spotList;
    private Button button;
    private Animation inAnim;
    private Animation outAnim;

    // tts
    private TextToSpeech tts;
    private HashMap<String, String> ttsMap = new HashMap();

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
        mMap.setOnMarkerClickListener(onMarkerClickListener);

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(onButtonClickListener);
        inAnim = AnimationUtils.loadAnimation(this, R.anim.in_from_under);
        outAnim = AnimationUtils.loadAnimation(this, R.anim.disapper_to_under);

        // instance initialize
        initialize();

        // Map Maker initialize
        // TODO:要動作確認 nishikawa_naoto 2015/11/03
        getPositionDatas();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO: 詳細を詰める
        if (requestCode == REQUEST_LOCATION_SET) {
            // ユーザのダイアログに対する応答をここで確認できる
            switch (resultCode) {
                case Activity.RESULT_OK:
                    Log.d(TAG, "Setting Success!");
                    break;
                case Activity.RESULT_CANCELED:
                    Log.e(TAG, "Setting Canceled!");
                    // TODO: 位置情報が使えない旨を伝える nishikawa_naoto 2015/11/06
                    break;
            }
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

    /**
     * ユーザーの位置情報利用設定を確認する
     */
    private void checkLocationPreference() {
        // TODO: 詳細を詰める nishikawa_naoto 2015/11/05
        // ユーザが必要な位置情報設定を満たしているか確認する
        PendingResult result = LocationServices.SettingsApi.checkLocationSettings(
                mGoogleClient,
                new LocationSettingsRequest.Builder().addLocationRequest(locationRequest).build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        fusedLocationProviderApi.requestLocationUpdates(mGoogleClient, locationRequest, MapActivity.this);
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            // ユーザに位置情報設定を変更してもらうためのダイアログを表示する
                            status.startResolutionForResult(MapActivity.this, REQUEST_LOCATION_SET);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // 位置情報が取得できず、なおかつその状態からの復帰も難しい時呼ばれるらしい
                        // TODO: 詳細を詰める nishikawa_naoto 2015/11/06
                        break;
                }
            }
        });
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
        // 現在位置表示の処理
        if (this.location == null) {
            animateCameraForTarget(mMap, location);
        }
        this.location = location;
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
                        // TODO: 表示倍率の修正 nishikawa_naoto 2015/11/06
                        .zoom(18.0f)
                        .build()));
    }

    /**
     * HTTP通信を行い必要なマーカーを用意する
     */
    public void getPositionDatas() {
        // TODO:別スレッドで行うことを検討する nishikawa_naoto 2015/11/03
        Log.d(TAG, "getSpotMarkers");

        // 会津バスバス停位置データ取得
        AppController.http(this, ApiContents.HTTP_GET, ApiContents.AIZUBUS_BUS_STOP_DATA, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("VOLLEY.onResponse", response.toString());

                        try {
                            int size = (int) response.get("count");
                            JSONArray dataArray = response.getJSONArray("data");
                            for (int i = 0; i < size; i++) {
                                JSONObject data = dataArray.getJSONObject(i);
                                String busStopNameKanji = (String) data.get("bus_stop_name_kanji");
                                String busStopNameKana = (String) data.get("bus_stop_name_kana");
                                double lat = (double) data.get("lat");
                                double lng = (double) data.get("lng");
                                BusStop busStop = new BusStop(busStopNameKanji, busStopNameKana, lat, lng);
                                createMapMarker(mMap, busStop);
                                busStopList.add(busStop);
                            }
                        } catch (JSONException e) {
                            // TODO:失敗時の処理を決める nishikawa_naoto 2015/11/03
                            e.printStackTrace();
                        }
                    }
                });

        // エコろん号バス停位置データ取得
        AppController.http(this, ApiContents.HTTP_GET, ApiContents.ECORON_BUS_STOP_DATA, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("VOLLEY.onResponse", response.toString());

                        try {
                            int size = (int) response.get("count");
                            JSONArray dataArray = response.getJSONArray("data");
                            for (int i = 0; i < size; i++) {
                                JSONObject data = dataArray.getJSONObject(i);
                                String busStopNameKanji = (String) data.get("bus_stop_name");
                                String busStopNameKana = (String) data.get("bus_stop_name_kana");
                                double lat = (double) data.get("lat");
                                double lng = (double) data.get("lng");
                                BusStop busStop = new BusStop(busStopNameKanji, busStopNameKana, lat, lng);
                                createMapMarker(mMap, busStop);
                                busStopList.add(busStop);
                            }
                        } catch (JSONException e) {
                            // TODO:失敗時の処理を決める nishikawa_naoto 2015/11/03
                            e.printStackTrace();
                        }
                    }
                });

        // 会津若松市公共施設位置データ取得
        AppController.http(this, ApiContents.HTTP_GET, ApiContents.AIZUWAKAMATSU_SPOT_DATA, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("VOLLEY.onResponse", response.toString());

                        try {
                            int size = (int) response.get("count");
                            JSONArray dataArray = response.getJSONArray("data");
                            for (int i = 0; i < size; i++) {
                                JSONObject data = dataArray.getJSONObject(i);
                                String spotNameKanji = (String) data.get("name");
                                double lat = (double) data.get("Latitude");
                                double lng = (double) data.get("Longitude");
                                Spot spot = new Spot(spotNameKanji, null, lat, lng);
                                createMapMarker(mMap, spot);
                                spotList.add(spot);
                            }
                        } catch (JSONException e) {
                            // TODO:失敗時の処理を決める nishikawa_naoto 2015/11/03
                            e.printStackTrace();
                        }
                    }
                });

        // 会津若松市観光史跡位置データ取得
        AppController.http(this, ApiContents.HTTP_GET, ApiContents.AIZUWAKAMATSU_HISTRIC_SPOT_DATA, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("VOLLEY.onResponse", response.toString());

                        try {
                            int size = (int) response.get("count");
                            JSONArray dataArray = response.getJSONArray("data");
                            for (int i = 0; i < size; i++) {
                                JSONObject data = dataArray.getJSONObject(i);
                                String spotNameKanji = (String) data.get("name");
                                String spotNameKana = (String) data.get("name_kana");
                                double lat = (double) data.get("lat");
                                double lng = (double) data.get("lng");
                                String description = (String) data.get("description");
                                HistricSpot spot = new HistricSpot(spotNameKanji, spotNameKana, lat, lng, description);
                                createMapMarker(mMap, spot);
                                spotList.add(spot);
                            }
                        } catch (JSONException e) {
                            // TODO:失敗時の処理を決める nishikawa_naoto 2015/11/03
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * マーカーを追加する
     * @param map GoogleMap
     * @param spot 施設
     */
    private <T extends Spot>void createMapMarker(GoogleMap map, T spot) {
        String name = spot.getKanji();
        if (name == null || name == "") {
            name = spot.getKana();
        }
        BitmapDescriptor buildingIcon = BitmapDescriptorFactory.fromResource(R.drawable.building);
        BitmapDescriptor histricalBuildingIcon = BitmapDescriptorFactory.fromResource(R.drawable.histricalbuilding);
        BitmapDescriptor busIcon = BitmapDescriptorFactory.fromResource(R.drawable.bus);

        // TODO: instanceOf を追加して分岐させる、マーカーを変える nishikawa_naoto 2015/11/06
//        map.addMarker(new MarkerOptions()
//                .position(spot.getLatLng())
//                .title(name)
//                .icon(BitmapDescriptorFactory
//                        .defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        if (spot instanceof HistricSpot) {
            map.addMarker(new MarkerOptions()
                    .position(spot.getLatLng())
                    .title(name)
                    .icon(histricalBuildingIcon));
        } else if (spot instanceof BusStop) {
            map.addMarker(new MarkerOptions()
                    .position(spot.getLatLng())
                    .title(name)
                    .icon(busIcon));
        } else {
            map.addMarker(new MarkerOptions()
                    .position(spot.getLatLng())
                    .title(name)
                    .icon(buildingIcon));
        }
    }

    /**
     * OnMarkerClickListener
     */
    private GoogleMap.OnMarkerClickListener onMarkerClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            tts.speak(marker.getTitle(), TextToSpeech.QUEUE_FLUSH, ttsMap);
            // TODO: animation のテスト nishikawa_naoto 2015/11/06
            if (button.getVisibility() == View.GONE) {
                button.startAnimation(inAnim);
                button.setVisibility(View.VISIBLE);
            }
            return false;
        }
    };

/***************************************************************************************************
****** Button **************************************************************************************
***************************************************************************************************/

    /**
     * OnButtonClickListener
     */
    private View.OnClickListener onButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getVisibility() == View.VISIBLE) {
                v.startAnimation(outAnim);
                v.setVisibility(View.GONE);
            }
        }
    };

/***************************************************************************************************
****** TextToSpeech ********************************************************************************
***************************************************************************************************/

    /**
     * OnInitListener
     */
    private TextToSpeech.OnInitListener onTTSInitListener = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
            if (TextToSpeech.SUCCESS == status) {
                Log.d(TAG, "TTS is initialized");
                tts.speak("あいずなびえようこそ",
                        TextToSpeech.QUEUE_FLUSH,
                        ttsMap);
            }
        }
    };

/***************************************************************************************************
**** Another Methods *******************************************************************************
***************************************************************************************************/

    /**
     * 初期化処理
     */
    private void initialize() {
        Log.d(TAG, "initialize");
        // list initialize
        busStopList = new ArrayList();
        spotList = new ArrayList();

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

        // tts
        ttsMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, null);
        tts = new TextToSpeech(this, onTTSInitListener);

        checkLocationPreference();
    }
}
