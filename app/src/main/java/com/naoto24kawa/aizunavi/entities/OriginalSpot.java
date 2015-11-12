package com.naoto24kawa.aizunavi.entities;

import android.location.Location;
import com.google.android.gms.maps.model.LatLng;

public class OriginalSpot extends Spot {

    private String description;

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
    public OriginalSpot(String kanji, String kana, double lat, double lng, String description) {
        super(kanji, kana, lat, lng);
        this.description = description;
    }

    /**
     * コンストラクタ
     * @param kanji 施設名（漢字）
     * @param kana 施設名（かな）
     * @param latLng 緯度経度
     */
    public OriginalSpot(String kanji, String kana, LatLng latLng, String description) {
        super(kanji, kana, latLng.latitude, latLng.longitude);
        this.description = description;
    }

    /**
     * コンストラクタ
     * @param kanji 施設名（漢字）
     * @param kana 施設名（かな）
     * @param location 緯度経度
     */
    public OriginalSpot(String kanji, String kana, Location location, String description) {
        super(kanji, kana, location.getLatitude(), location.getLongitude());
        this.description = description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}
