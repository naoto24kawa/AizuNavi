package com.naoto24kawa.aizunavi.network;

public class ApiContents {
    // InitialSetting N2TTS導入用
    public static final String GOOGLEPLAY_BASE_URI = "market://details?id=";
    public static final String N2TTS_PACKAGE_NAME = "jp.kddilabs.n2tts";

    // VOLLEY methods
    public static final int HTTP_GET = 0;
    public static final int HTTP_POST = 1;

    // URLs
    /**
     * 会津バスバス停位置データ
     * 参照URL
     * http://www.data4citizen.jp/app/users/openDataTop/show/O_BUS_STOP_DATA
     */
    public static final String AIZUBUS_BUS_STOP_DATA = "http://www.data4citizen.jp/app/users/openDataOutput/json/get/O_BUS_STOP_DATA";
    /**
     * エコろん号バス停位置データ
     * 参照URL
     * http://www.data4citizen.jp/app/users/openDataTop/show/O_ECORON_BUS_STOP
     */
    public static final String ECORON_BUS_STOP_DATA = "http://www.data4citizen.jp/app/users/openDataOutput/json/get/O_ECORON_BUS_STOP";
    /**
     * 会津若松市公共施設マップデータ
     * 参照URL
     * http://www.data4citizen.jp/app/users/openDataTop/show/aizuwakamatsu_map
     */
    public static final String AIZUWAKAMATSU_SPOT_DATA = "http://www.data4citizen.jp/app/users/openDataOutput/json/get/aizuwakamatsu_map";
    /**
     * 会津若松市道の駅・町の駅データ
     * 参照URL
     * http://www.data4citizen.jp/app/users/openDataTop/show/o_aizu_station
     */
    public static final String AIZUWAKAMATSU_STATION_DATA = "http://www.data4citizen.jp/app/users/openDataOutput/json/get/o_aizu_station";
    /**
     * 会津若松市観光史跡データ
     * 参照URL
     * http://www.data4citizen.jp/app/users/openDataTop/show/O_HISTRIC_SITE
     */
    public static final String AIZUWAKAMATSU_HISTRIC_SPOT_DATA = "http://www.data4citizen.jp/app/users/openDataOutput/json/get/O_HISTRIC_SITE";
    /**
     * 会津若松市ビーコン設置情報
     * 参照URL
     * http://www.data4citizen.jp/app/users/openDataTop/show/O_AIZU_BEACON
     */
    public static final String AIZUWAKAMATSU_BEACON_DATA = "http://www.data4citizen.jp/app/users/openDataTop/json/O_AIZU_BEACON";
}
