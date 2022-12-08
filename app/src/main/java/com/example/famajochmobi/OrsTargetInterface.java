package com.example.famajochmobi;

import android.content.Context;

import com.android.volley.VolleyError;

import org.json.JSONObject;

public interface OrsTargetInterface {
    void processOrsResult(JSONObject response);

    void processOrsError(VolleyError error);

    Context appContext();

}
