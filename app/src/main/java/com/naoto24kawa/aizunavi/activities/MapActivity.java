package com.naoto24kawa.aizunavi.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

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
import com.google.gson.Gson;
import com.naoto24kawa.aizunavi.R;
import com.naoto24kawa.aizunavi.entities.BusStop;
import com.naoto24kawa.aizunavi.entities.HistricSpot;
import com.naoto24kawa.aizunavi.entities.OriginalSpot;
import com.naoto24kawa.aizunavi.entities.Spot;
import com.naoto24kawa.aizunavi.models.Consts;
import com.naoto24kawa.aizunavi.network.ApiContents;
import com.naoto24kawa.aizunavi.network.AppController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.android.volley.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class MapActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        OnMapReadyCallback,
        GoogleMap.OnCameraChangeListener,
        GoogleMap.OnMapLongClickListener {

/***************************************************************************************************
**** Instances *************************************************************************************
***************************************************************************************************/

    // set TAG contents
    private static final String TAG = MapActivity.class.getSimpleName();
    private static final int REQUEST_LOCATION_SET = 0;

    // Preference
    private SharedPreferences sp;
    private static final String SAVED_DATA = "saved_data";
    private static final String JSON_DATA = "json_data";
    private Map<Marker, Spot> savedSpotMap;

    // To need Initialize instance
    private GoogleApiClient mGoogleClient;
    private LocationRequest locationRequest;

    // instance of this activity
    private MapFragment mMapFragment;
    private GoogleMap mMap;
    private Map<Marker, Spot> markerMap;
    private FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;
    private Location location;
    private LatLng oldLatLng;
    private boolean markerClickFlg = false;
    private List<BusStop> busStopList;
    private List<Spot> spotList;
    private HistricSpot descriptionSpot;
    private Spot lastTapped = new Spot();

    // layout parts
    private LinearLayout buttons;
    private Button destButton;
    private Button descButton;
    private ImageButton busButton;
    private ImageButton histButton;
    private ImageButton buildButton;
    private Animation inAnim;
    private Animation outAnim;

    // tts
    private TextToSpeech tts;
    private HashMap<String, String> ttsMap = new HashMap();
    private static final int SPOT = 0;
    private static final int BUS_STOP = 1;
    private static final int DESCRIPTION = 2;
    private static final int ETC = 3;

/***************************************************************************************************
**** Activity Cycles *******************************************************************************
***************************************************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        // view initialize
        setContentView(R.layout.activity_map);

        // initialize view parts
        viewInit();

        // instance initialize
        initialize();

        // Map Maker initialize
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

        // SharedPreferenceに登録したマーカーを保存する
        Gson gson = new Gson();
        sp.edit().putString(JSON_DATA, gson.toJson(savedSpotMap, HashMap.class).toString()).commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LOCATION_SET) {
            // ユーザのダイアログに対する応答をここで確認できる
            switch (resultCode) {
                case Activity.RESULT_OK : {
                    Log.d(TAG, "Setting Success!");
                    break;
                }
                case Activity.RESULT_CANCELED : {
                    Log.e(TAG, "Setting Canceled!");
                    Toast.makeText(this, R.string.unable_current_location, Toast.LENGTH_LONG).show();
                    break;
                }
            }
        }
    }

/***************************************************************************************************
**** GoogleApiClients ******************************************************************************
***************************************************************************************************/

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");
        Toast.makeText(this, R.string.success_gps_service, Toast.LENGTH_LONG).show();

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
        Toast.makeText(this, R.string.suspended_gps_service, Toast.LENGTH_LONG).show();
        Toast.makeText(this, R.string.unable_current_location, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed : " + connectionResult.getErrorMessage());
        Toast.makeText(this, R.string.error_gps_service, Toast.LENGTH_LONG).show();
        Toast.makeText(this, R.string.unable_current_location, Toast.LENGTH_LONG).show();
    }

    /**
     * ユーザーの位置情報利用設定を確認する
     */
    private void checkLocationPreference() {
        // ユーザが必要な位置情報設定を満たしているか確認する
        PendingResult result = LocationServices.SettingsApi.checkLocationSettings(
                mGoogleClient,
                new LocationSettingsRequest.Builder().addLocationRequest(locationRequest).build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS: {
                        fusedLocationProviderApi.requestLocationUpdates(mGoogleClient, locationRequest, MapActivity.this);
                        break;
                    }

                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED: {
                        try {
                            // ユーザに位置情報設定を変更してもらうためのダイアログを表示する
                            status.startResolutionForResult(MapActivity.this, REQUEST_LOCATION_SET);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                        break;
                    }

                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE: {
                        // 位置情報が取得できず、なおかつその状態からの復帰も難しい時呼ばれる
                        Toast.makeText(MapActivity.this, R.string.unable_current_location, Toast.LENGTH_LONG).show();
                        break;
                    }
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

    @Override
    public void onMapLongClick(LatLng latLng) {
        // TODO: マーカー追加処理 nishikawa_naoto 2015/11/09
        Spot spot = new Spot();
        String title = "";

        Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(title)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

//        savedSpotMap.put(marker, spot);
        markerMap.put(marker, spot);
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
                        .zoom(9.0f)
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
                        .zoom(9.0f)
                        .build()));
    }

    /**
     * HTTP通信を行い必要なマーカーを用意する
     */
    public void getPositionDatas() {
        // TODO:別スレッドで行うことを検討する nishikawa_naoto 2015/11/03
        Log.d(TAG, "getSpotMarkers");

        // Preferenceから保存された位置データ取得する
        Gson gson = new Gson();
        String json = sp.getString(JSON_DATA, null);
        if (json != null && !json.equals("")) {
            savedSpotMap = gson.fromJson(json, HashMap.class);
            for (Marker marker : savedSpotMap.keySet()) {
                mMap.addMarker(new MarkerOptions()
                        .position(marker.getPosition())
                        .title(marker.getTitle())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            }
        }

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
        BitmapDescriptor icon;

        // 名前設定
        if (name == null || name.equals("")) {
            name = spot.getKana();
        }

        // アイコン設定
        if (spot instanceof HistricSpot) {
            icon = BitmapDescriptorFactory.fromResource(R.drawable.histricalbuilding);
        } else if (spot instanceof BusStop) {
            icon = BitmapDescriptorFactory.fromResource(R.drawable.bus);
        } else {
            icon = BitmapDescriptorFactory.fromResource(R.drawable.building);
        }

        Marker marker = map.addMarker(new MarkerOptions()
                            .position(spot.getLatLng())
                            .title(name)
                            .icon(icon));

        // マーカー追加
        markerMap.put(marker, spot);
    }

    /**
     * OnMarkerClickListener
     */
    private GoogleMap.OnMarkerClickListener onMarkerClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            markerClickFlg = true;

            Spot spot = markerMap.get(marker);

            if (lastTapped.equals(spot)) {
                if (spot instanceof OriginalSpot) {
                    marker.remove();
                    // TODO:削除処理を追加する 西川直登 2015/11/10
//                savedSpotMap.remove(marker);
                    markerMap.remove(marker);

                    viewController(false, false, false);

                    return false;
                }
            }

            lastTapped = spot;


            String message = spot.getKana();
            if (message == null || message.equals("")) {
                message = spot.getKanji();
            }

            if (spot instanceof BusStop) {
                speakTTS(message, BUS_STOP);
            } else {
                speakTTS(message, SPOT);
            }

            if (spot instanceof HistricSpot) {
                descriptionSpot = (HistricSpot) spot;
                viewController(true, true, true);
            } else {
                viewController(true, false, true);
            }

            return false;
        }
    };

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        if(oldLatLng != null){
            LatLng newPos = cameraPosition.target;

            if(!oldLatLng.equals(newPos) && !markerClickFlg){
                viewController(false, false, false);
            }
        }
        oldLatLng = cameraPosition.target;
        markerClickFlg = false;
    }

/***************************************************************************************************
****** Button **************************************************************************************
***************************************************************************************************/

    /**
     * OnDestButtonClickListener
     */
    private View.OnClickListener onDestButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            viewController(false, false, false);

            if (destButton.getVisibility() == View.VISIBLE) {
                // TODO: 経路検索ロジックの実装 nishikawa_naoto 2015/11/09
            }
        }
    };

    /**
     * OnDescButtonClickListener
     */
    private View.OnClickListener onDescButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            viewController(false, false, false);

            if (descButton.getVisibility() == View.VISIBLE) {
                speakTTS(descriptionSpot.getDescription(), DESCRIPTION);
            }
        }
    };

    /**
     * onBusButtonClickListener
     */
    private View.OnClickListener onBusButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            for (Marker marker : markerMap.keySet()) {
                if (markerMap.get(marker).getClass().equals(BusStop.class)) {
                    if (marker.isVisible()) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        }
    };

    /**
     * onHistButtonClickListener
     */
    private View.OnClickListener onHistButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            for (Marker marker : markerMap.keySet()) {
                if (markerMap.get(marker).getClass().equals(HistricSpot.class)) {
                    if (marker.isVisible()) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        }
    };

    /**
     * onBuildButtonClickListener
     */
    private View.OnClickListener onBuildButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            for (Marker marker : markerMap.keySet()) {
                if (!markerMap.get(marker).getClass().equals(BusStop.class) &&
                        !markerMap.get(marker).getClass().equals(HistricSpot.class)) {
                    if (marker.isVisible()) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
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
                speakTTS(getString(R.string.welcome), ETC);
            }
        }
    };

    /**
     * 文字を付加して文章を読み上げます
     * @param message 文章
     * @param type 種類
     */
    private void speakTTS(String message, int type) {
        final String thisIs = "ここは、";
        final String end = "、です。";
        final String busStop = "バス停、";

        switch (type) {
            case SPOT : {
//                message = thisIs + message + end;
                break;
            }
            case BUS_STOP : {
                message = busStop + message;
                break;
            }
            case DESCRIPTION : {
                break;
            }
            case ETC : {
                break;
            }
        }

        tts.speak(message, TextToSpeech.QUEUE_FLUSH, ttsMap);
    }

    /**
     * 読み上げの始まりと終わりを取得する
     */
    private void setTTSListener(){
        // android version more than 15th
        if (Build.VERSION.SDK_INT >= 15) {
            int listenerResult = tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onDone(String utteranceId) {
                    Log.d(TAG, "progress on Done " + utteranceId);
                }

                @Override
                public void onError(String utteranceId) {
                    Log.d(TAG, "progress on Error " + utteranceId);
                    Toast.makeText(MapActivity.this, R.string.error_tts, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onStart(String utteranceId) {
                    Log.d(TAG, "progress on Start " + utteranceId);
                }

            });

            if (listenerResult != TextToSpeech.SUCCESS) {
                Log.e(TAG, "failed to add utterance progress listener");
            }
        } else {
            // less than 15th
            int listenerResult = tts.setOnUtteranceCompletedListener(new TextToSpeech.OnUtteranceCompletedListener() {
                @Override
                public void onUtteranceCompleted(String utteranceId) {
                    Log.d(TAG, "progress on Completed " + utteranceId);
                }
            });

            if (listenerResult != TextToSpeech.SUCCESS) {
                Log.e(TAG, "failed to add utterance completed listener");
                Toast.makeText(this, R.string.error_tts, Toast.LENGTH_LONG).show();
            }
        }
    }

/***************************************************************************************************
**** Another Methods *******************************************************************************
***************************************************************************************************/

    /**
     * 初期化処理
     */
    private void initialize() {
        Log.d(TAG, "initialize");
        sp = getSharedPreferences(SAVED_DATA, MODE_PRIVATE);

        // list initialize
        busStopList = new ArrayList();
        spotList = new ArrayList();
        markerMap = new HashMap<Marker, Spot>();

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
        setTTSListener();

        checkLocationPreference();
    }

    /**
     * Viewの初期化処理
     */
    private void viewInit() {
        mMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);
        mMapFragment.getMapAsync(this);
        mMap = mMapFragment.getMap();
        mMap.setOnMarkerClickListener(onMarkerClickListener);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnCameraChangeListener(this);
        animateCameraForTarget(mMap, new LatLng(Consts.AIZUWAKAMATSU_LAT, Consts.AIZUWAKAMATSU_LNG));

        buttons = (LinearLayout) findViewById(R.id.action_buttons);
        destButton = (Button) findViewById(R.id.destination_button);
        destButton.setOnClickListener(onDestButtonClickListener);
        descButton = (Button) findViewById(R.id.description_button);
        descButton.setOnClickListener(onDescButtonClickListener);
        busButton = (ImageButton) findViewById(R.id.bus_button);
        busButton.setOnClickListener(onBusButtonClickListener);
        histButton = (ImageButton) findViewById(R.id.hist_button);
        histButton.setOnClickListener(onHistButtonClickListener);
        buildButton = (ImageButton) findViewById(R.id.build_button);
        buildButton.setOnClickListener(onBuildButtonClickListener);
        inAnim = AnimationUtils.loadAnimation(this, R.anim.in_from_under);
        outAnim = AnimationUtils.loadAnimation(this, R.anim.disapper_to_under);
    }

    /**
     * Viewの表示状態を操作します
     * @param buttons 親View
     * @param desc 子View
     * @param dest 子View
     */
    private void viewController(boolean buttons, boolean desc, boolean dest) {
        // 解説ボタンの表示設定
        if (desc) {
            if (this.buttons.getVisibility() == View.GONE) {
                if (this.descButton.getVisibility() == View.GONE) {
                    this.descButton.setVisibility(View.VISIBLE);
                }
            } else {
                if (this.descButton.getVisibility() == View.GONE) {
                    this.descButton.startAnimation(inAnim);
                    this.descButton.setVisibility(View.VISIBLE);
                }
            }
        } else {
            if (this.buttons.getVisibility() == View.VISIBLE) {
                if (this.descButton.getVisibility() == View.VISIBLE) {
                    if (buttons) {
                        this.descButton.startAnimation(outAnim);
                        this.descButton.setVisibility(View.GONE);
                    }
                }
            } else {
                this.descButton.setVisibility(View.GONE);
            }
        }

        // 経路検索ボタンの表示設定
        if (dest) {
            if (this.buttons.getVisibility() == View.GONE) {
                if (this.destButton.getVisibility() == View.GONE) {
                    this.destButton.setVisibility(View.VISIBLE);
                }
            }
        } else {
            if (this.buttons.getVisibility() == View.GONE) {
                this.destButton.setVisibility(View.GONE);
            }
        }

        // 親ボタンの表示設定
        if (buttons) {
            if (this.buttons.getVisibility() == View.GONE) {
                this.buttons.startAnimation(inAnim);
                this.buttons.setVisibility(View.VISIBLE);
            }
        } else {
            this.buttons.startAnimation(outAnim);
            this.buttons.setVisibility(View.GONE);
        }
    }
}
