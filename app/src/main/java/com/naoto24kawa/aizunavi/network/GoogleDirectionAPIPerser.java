package com.naoto24kawa.aizunavi.network;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.naoto24kawa.aizunavi.entities.Spot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GoogleDirectionAPIPerser {

    public GoogleDirectionAPIPerser() {
        super();
    }

    /**
     * GoogleDirectionAPI JSONParser
     * @param jsonObject
     * @return List<LatLng>
     */
    public List<LatLng> perse(JSONObject jsonObject) {
        List<List<List<LatLng>>> routes = new ArrayList<>();

        try {
            JSONArray jsonRoutes = jsonObject.getJSONArray("routes");

            for (int i = 0; i < jsonRoutes.length(); i++) {
                JSONObject route = (JSONObject) jsonRoutes.get(i);
                JSONArray jsonLegs = route.getJSONArray("legs");
                List path = new ArrayList<List<LatLng>>();

                for (int j = 0; j < jsonLegs.length(); j++) {
                    JSONObject leg = (JSONObject) jsonLegs.get(i);
                    JSONArray jsonSteps = leg.getJSONArray("steps");

                    for (int k = 0; k < jsonSteps.length(); k++) {
                        JSONObject step = (JSONObject) jsonSteps.get(i);
                        String polyline = (((JSONObject)(step.get("polyline"))).get("points")).toString();
                        List<LatLng> list = decode(polyline);
                        path.add(list);
                    }
                }
                routes.add(path);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        List<LatLng> resultList = new ArrayList<>();

        for (List<List<LatLng>> LatLngList : routes) {
            for (List<LatLng> LatLngs : LatLngList) {
                for (LatLng latLng : LatLngs) {
                    resultList.add(latLng);
                }
            }
        }

        return resultList;
    }

    /**
     * 座標データをデコード
     */
    private List<LatLng> decode(String code) {

        List<LatLng> latLng = new ArrayList<LatLng>();
        int index = 0, len = code.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = code.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = code.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            latLng.add(p);
        }

        return latLng;
    }

    public String createUrl(int travelType, Location origin, Spot destination) {
        String mode = "driving"; // default
        switch (travelType) {
            case ApiContents.TRAVEL_MODE_DRIVEING : {
                mode = "driving";
                break;
            }
        }

        String originPos = "origin=" + Double.toString(origin.getLatitude()) + "," + Double.toString(origin.getLongitude());
        String destinationPos = "destination=" + Double.toString(destination.getLat()) + "," + Double.toString(destination.getLng());

        String sensor = "sensor=false";
        String params = originPos + "&" + destinationPos + "&" + sensor + "&language=ja" + "&mode=" + mode;

        String output = "json";

        return  "https://maps.googleapis.com/maps/api/directions/" + output + "?" + params;
    }
}
