package com.naoto24kawa.aizunavi.entities;

import interfaces.BaseModel;

/**
 * バス停モデル
 */
public class BusStop extends BaseModel {

    /** バス停名（漢字） */
    private String busStopKanji;

    /** バス停名（かな） */
    private String busStopKana;

    /** 緯度 */
    private double lat;

    /** 経度 */
    private double lng;

    /**
     * コンストラクタ
     */
    public BusStop() {
        // 初期化処理
        this.busStopKanji = "";
        this.busStopKana = "";
        this.lat = 0;
        this.lng = 0;
    }

    /**
     * コンストラクタ
     * @param kanji バス停名（漢字）
     * @param kana バス停名（かな）
     * @param lat 緯度
     * @param lng 経度
     */
    public BusStop(String kanji, String kana, double lat, double lng) {
        // 初期化処理
        this.busStopKanji = kanji;
        this.busStopKana = kana;
        this.lat = lat;
        this.lng = lng;
    }

    /**
     * バス停名（漢字）を設定する
     * @param kanji バス停名（漢字）
     */
    public void setBusStopKanji(String kanji) {
        this.busStopKanji = kanji;
    }

    /**
     * バス停名（漢字）を取得する
     * @return バス停名（漢字）
     */
    public String getBusStopKanji() {
        return this.busStopKanji;
    }

    /**
     * バス停名（かな）を設定する
     * @param kana バス停名（かな）
     */
    public void setBusStopKana(String kana) {
        this.busStopKana = kana;
    }

    /**
     * バス停名（かな）を取得する
     * @return バス停名（かな）
     */
    public String getBusStopKana() {
        return this.busStopKana;
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
     * 経度を取得する
     * @return 経度
     */
    public double getLng() {
        return this.lng;
    }

    @Override
    protected boolean isExist() {
        // バス停名が存在するか確認する
        if (this.busStopKanji.isEmpty() && this.busStopKana.isEmpty()) {
            return false;
        }
        // 緯度経度が存在するか確認する
        if (this.lat == 0 || this.lng == 0) {
            return false;
        }
        return true;
    }
}
