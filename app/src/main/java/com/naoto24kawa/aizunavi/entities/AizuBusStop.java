package com.naoto24kawa.aizunavi.entities;

public class AizuBusStop extends BusStop {

    private String bus_stop_name_kanji;

    private String bus_stop_name_kana;

    private double lat;

    private double lng;

    public AizuBusStop() {
        super();
    }

    public AizuBusStop(String bus_stop_name_kanji, String bus_stop_name_kana, double lat, double lng) {
        super(bus_stop_name_kanji, bus_stop_name_kana, lat, lng);
    }
}
