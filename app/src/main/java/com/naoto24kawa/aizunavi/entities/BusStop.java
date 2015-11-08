package com.naoto24kawa.aizunavi.entities;

/**
 * バス停モデル
 */
public class BusStop extends Spot {

    /**
     * コンストラクタ
     */
    public BusStop() {
        super();
    }

    /**
     * コンスタラクタ
     * @param kanji バス停名（漢字）
     * @param kana バス停名　（かな）
     * @param lat 緯度
     * @param lng 経度
     */
    public BusStop(String kanji, String kana, double lat, double lng) {
        super(kanji, kana, lat, lng);
    }
}
