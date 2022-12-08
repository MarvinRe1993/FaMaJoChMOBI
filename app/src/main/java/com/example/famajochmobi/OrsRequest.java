package com.example.famajochmobi;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class OrsRequest {

String profile;
LatLng startPos;
LatLng targetPos;
OrsTargetInterface responseTarget;
private final String api_key = "5b3ce3597851110001cf6248f068288ba04e4c8983eb90aad2818edf";
    private final String baseUrl = "https://api.openrouteservice.org/v2/directions/";

    String url;

    public OrsRequest(String profile, LatLng startPos, LatLng targetPos, OrsTargetInterface responseTarget) {
        this.profile = profile;
        this.startPos = startPos;
        this.targetPos = targetPos;
        this.responseTarget = responseTarget;

        url = baseUrl + profile+ "/geojson";

        doRequest();
    }


    public void doRequest(){
        JSONObject orsParameter = new JSONObject();

        JSONArray KordArray = new JSONArray();
        JSONArray startKordArray = new JSONArray();
        JSONArray targetKordArray = new JSONArray();

        try{
            startKordArray.put(8.681495);
            startKordArray.put(49.41461);
        } catch (JSONException e){
            e.printStackTrace();
        }

        try{
            targetKordArray.put(8.686507);
            targetKordArray.put(49.41943);
        } catch (JSONException e){
            e.printStackTrace();
        }
        KordArray.put(startKordArray);
        KordArray.put(targetKordArray);



        try {
            orsParameter.put("coordinates", KordArray);
        } catch (JSONException e) {

        }
        JSONObject alternativeRoutesObject = new JSONObject();
        try {

            alternativeRoutesObject.put("share_factor", 0.5);

            alternativeRoutesObject.put("target_count", 2);

            alternativeRoutesObject.put("weight_factor", 1.4);

        } catch (JSONException e){

        }

        try {
            orsParameter.put("alternative_routes", alternativeRoutesObject);
        } catch (JSONException e) {

        }


    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
            (Request.Method.POST, url, orsParameter, response -> responseTarget.processOrsResult(response), error -> responseTarget.processOrsError(error)) {
        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            HashMap<String, String> superHeaders = new
                    HashMap<>(super.getHeaders());
            superHeaders.put("Authorization", api_key);
            return Collections.unmodifiableMap(superHeaders);
        }
    };


// Access the RequestQueue through your singleton class.
        MySingleton.getInstance(responseTarget.appContext()).addToRequestQueue(jsonObjectRequest);

    }







}