package com.naoto24kawa.aizunavi.entities;

public class EcoronBusStop extends BusStop {

    private String bus_stop_name;

    private String bus_stop_name_kana;

    private double lat;

    private double lng;

    public EcoronBusStop() {
        super();
    }

    public EcoronBusStop(String bus_stop_name, String bus_stop_name_kana, double lat, double lng) {
        super(bus_stop_name, bus_stop_name_kana, lat, lng);
    }
}
