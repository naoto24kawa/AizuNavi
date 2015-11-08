package com.naoto24kawa.aizunavi.entities;

public class HistricSpot extends Spot {

    private String name;

    private String name_kana;

    private double lat;

    private double lng;

    /** 施設概要 */
    private String description;

    /**
     * コンストラクタ
     * @param kanji 施設名（漢字）
     * @param kana 施設名（かな）
     * @param lat 緯度
     * @param lng 経度
     * @param description 施設概要
     */
    public HistricSpot(String kanji, String kana, double lat, double lng, String description) {
        super(kanji, kana, lat, lng);
        this.description = description;
    }

    /**
     * コンストラクタ
     */
    public HistricSpot() {
        super();
        this.description = "";
    }

    /**
     * 施設概要を設定する
     * @param description 施設概要
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 施設概要を取得する
     * @return 施設概要
     */
    public String getDescription() {
        return this.description;
    }
}
