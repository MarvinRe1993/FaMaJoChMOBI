package com.example.famajochmobi;

import android.content.Context;

import com.android.volley.VolleyError;

import org.json.JSONObject;

public interface OrsTargetInterface {
    void processOrsResult(String profile, JSONObject response);

    void processOrsError(String profile, VolleyError error);

    Context appContext();

}
