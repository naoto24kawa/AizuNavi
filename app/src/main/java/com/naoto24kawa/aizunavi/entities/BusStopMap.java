package com.naoto24kawa.aizunavi.entities;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import interfaces.BaseModel;

/**
 * バス停マップモデル
 */
public class BusStopMap extends BaseModel {

    /** バス停一覧 */
    private Map<String, String> map;

    /**
     * コンストラクタ
     */
    public BusStopMap() {
        this.map = new HashMap<String, String>();
    }

    /**
     * コンストラクタ
     * @param list バス停一覧
     */
    public BusStopMap(List<String> list) {
        this.map = new HashMap<String, String>();
        for (String busStop : list) {
            map.put(busStop, busStop);
        }
    }

    /**
     * マップにキーと値を設置する
     * @param key キー
     * @param value 値
     */
    public void put(String key, String value) {
        this.map.put(key, value);
    }

    /**
     * マップにキーを与えて値を取得する
     * @param key キー
     * @return value 値
     */
    public String get(String key) {
        return this.map.get(key);
    }

    /**
     * マップを取得する
     * @return マップ
     */
    public Map getAll() {
        return this.map;
    }

    @Override
    protected boolean isExist() {
        return !this.map.isEmpty();
    }
}
