package com.naoto24kawa.aizunavi.entities;

import android.location.Location;
import com.google.android.gms.maps.model.LatLng;

public class OriginalSpot extends Spot {

    /**
     * コンストラクタ
     */
    public OriginalSpot() {
        super();
    }

    /**
     * コンストラクタ
     * @param kanji 施設名（漢字）
     * @param kana 施設名（かな）
     * @param lat 緯度
     * @param lng 経度
     */
    public OriginalSpot(String kanji, String kana, double lat, double lng) {
        super(kanji, kana, lat, lng);
    }

    /**
     * コンストラクタ
     * @param kanji 施設名（漢字）
     * @param kana 施設名（かな）
     * @param latLng 緯度経度
     */
    public OriginalSpot(String kanji, String kana, LatLng latLng) {
        super(kanji, kana, latLng.latitude, latLng.longitude);
    }

    /**
     * コンストラクタ
     * @param kanji 施設名（漢字）
     * @param kana 施設名（かな）
     * @param location 緯度経度
     */
    public OriginalSpot(String kanji, String kana, Location location) {
        super(kanji, kana, location.getLatitude(), location.getLongitude());
    }
}
