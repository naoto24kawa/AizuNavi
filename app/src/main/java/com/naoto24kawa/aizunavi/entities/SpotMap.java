package com.naoto24kawa.aizunavi.entities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.naoto24kawa.aizunavi.interfaces.BaseModel;

public class SpotMap extends BaseModel {

    /** 施設一覧 */
    Map<String, String> map;

    /**
     * コンストラクタ
     */
    public SpotMap() {
        map = new HashMap<String, String>();
    }

    /**
     * コンストラクタ
     */
    public SpotMap(List<String> list) {
        map = new HashMap<String, String>();
        for (String spot : list) {
            map.put(spot, spot);
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
        return !map.isEmpty();
    }
}
