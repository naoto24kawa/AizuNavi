package com.naoto24kawa.aizunavi.entities;

import com.google.android.gms.maps.model.LatLng;
import com.naoto24kawa.aizunavi.interfaces.BaseModel;

public class Spot extends BaseModel {

    /** 施設名（漢字） */
    private String spotKanji;

    /** 施設名（かな） */
    private String spotKana;

    /** 緯度 */
    private double lat;

    /** 経度 */
    private double lng;

    /**
     * コンストラクタ
     */
    public Spot() {
        this.spotKanji = "";
        this.spotKana = "";
        this.lat = 0;
        this.lng = 0;
    }

    /**
     * コンストラクタ
     */
    public Spot(String kanji, String kana, double lat, double lng) {
        this.spotKanji = kanji;
        this.spotKana = kana;
        this.lat = lat;
        this.lng = lng;
    }

    /**
     * 緯度経度を取得します
     * @return 緯度経度
     */
    public LatLng getLatLng() {
        return new LatLng(this.lat, this.lng);
    }

    /**
     * 施設名（漢字）を設定する
     * @param kanji 施設名（漢字）
     */
    public void setSpotKanji(String kanji) {
        this.spotKanji = kanji;
    }

    /**
     * 施設名（漢字）を取得する
     * @return 施設名（漢字）
     */
    public String getSpotKanji() {
        return this.spotKanji;
    }

    /**
     * 施設名（かな）を設定する
     * @param kana 施設名（かな）
     */
    public void setSpotKana(String kana) {
        this.spotKana = kana;
    }

    /**
     * 施設名（かな）を取得する
     * @return 施設名（かな）
     */
    public String getSpotKana() {
        return this.spotKana;
    }

    /**
     * 緯度を設定する
     * @param lat 緯度
     */
    public void setLat(double lat) {
        this.lat = lat;
    }

    /**
     * 緯度を取得する
     * @return 緯度
     */
    public double getLat() {
        return this.lat;
    }

    /**
     * 経度を設定する
     * @param lng 経度
     */
    public void setLng(double lng) {
        this.lng = lng;
    }

    /**
     * 経度を設定する
     * @return 経度
     */
    public double getLng() {
        return this.lng;
    }

    @Override
    protected boolean isExist() {
        // バス停名が存在するか確認する
        if (this.spotKanji.isEmpty() && this.spotKana.isEmpty()) {
            return false;
        }
        // 緯度経度が存在するか確認する
        if (this.lat == 0 || this.lng == 0) {
            return false;
        }
        return true;
    }
}
