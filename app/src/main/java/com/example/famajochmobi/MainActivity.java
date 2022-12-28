package com.example.famajochmobi;

import androidx.annotation.ColorLong;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.VolleyError;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.data.Layer;
import com.google.maps.android.data.geojson.GeoJsonFeature;
import com.google.maps.android.data.geojson.GeoJsonLayer;
import com.google.maps.android.data.geojson.GeoJsonLineString;
import com.google.maps.android.data.geojson.GeoJsonLineStringStyle;
import com.google.maps.android.data.geojson.GeoJsonPointStyle;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, OrsTargetInterface {


    LatLng start;
    LatLng target;
    GoogleMap googleMap;
    String profilCar = "driving-car";
    String profilWalking = "foot-walking";
    //cycling-regular gibt nicht immer 3 Routen zurück, deshalb cycling-road
    //String profilBike = "cycling-regular";
    String profilBike = "cycling-road";

    // Variablen für Anfrageverwaltung
    boolean start_input;
    boolean ziel_input;
    boolean input;
    boolean car_pressed = false;
    boolean bike_pressed = false;
    boolean walk_pressed = false;

    //Definition Marker Start - Ziel
    Marker markerStart;
    Marker markerZiel;

    GeoJsonLayer bestLayerCar = null;
    GeoJsonLayer bestLayerBike = null;
    GeoJsonLayer bestLayerWalk = null;

    GeoJsonLayer layerCar = null;
    GeoJsonLayer layerBike = null;
    GeoJsonLayer layerWalk = null;



    ProgressDialog progressBar;
    private int progressBarStatus = 0;
    private Handler progressBarHandler = new Handler();
    private long fileSize = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.maps);
        mapFragment.getMapAsync(this);




    }




    // Standort wird ermittelt nach dem Klick auf den Button
    protected void getLocationPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            updateLocation();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION},0);
            }
        }
    }

    // Methode wird immer dann aufgerufen wenn der Nutzer sich entschieden hat den Button zu drücken. Hier wird die Erlaubnis eingeholt.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            updateLocation();
        }

    }


    @SuppressLint("MissingPermission")
    protected void updateLocation(){
        googleMap.setMyLocationEnabled(true);

    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;

        getLocationPermission();

        // Deklarieren der Start und Ziel und Suche Buttons
        Button btnStart = this.findViewById(R.id.btn_start);
        Button btnZiel = this.findViewById(R.id.btn_ziel);
        Button btnSearch = this.findViewById(R.id.btn_search);
        Button btnWalk = this.findViewById(R.id.btn_walk);
        Button btnBike = this.findViewById(R.id.btn_fahrrad);
        Button btnCar = this.findViewById(R.id.btn_car);


        // Klick auf Start Button
        btnStart.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View view) {
                if (markerStart != null) markerStart.remove();
                input = true;
                start_input = false;
                btnStart.setBackgroundColor(Color.parseColor(getString(R.color.Start_Ziel_pressed)));
                Toast.makeText(getApplicationContext(), "Startpunkt auf der Karte wählen!", Toast.LENGTH_LONG).show();

                if(input == true){
                // Long Click für Koordinaten
                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

                    @Override
                    public void onMapClick(LatLng start) {
                        if (start_input == false) {
                            //Toast.makeText(getApplicationContext(), start.toString(), Toast.LENGTH_SHORT).show();
                            //  String message = edt_start.getText().toString();

                            Log.i("Startpunkt", String.valueOf(start));

                            // Übergabe an startPos
                            OrsRequest.startPos = start;

                            // Ausgabe der Koordinaten für Textfeld
                            TextView textView = findViewById(R.id.txt_start);
                            textView.setText("Latitude: " + String.valueOf(Math.round(OrsRequest.startPos.latitude * 100000) / 100000.0) + "  Longitude: " + String.valueOf(Math.round(OrsRequest.startPos.longitude * 100000) / 100000.0));

                            if (OrsRequest.startPos != null) {
                                btnStart.setBackgroundColor(Color.parseColor(getString(R.color.Start_Button)));
                                start_input = true;
                                markerStart = googleMap.addMarker(new MarkerOptions().position(OrsRequest.startPos).title("Start"));
                            }
                        }
                    }
                });
            }
            }
        });

        // Klick auf Ziel Button
        btnZiel.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View view) {
                if (markerZiel != null) markerZiel.remove();

                input = true;
                ziel_input = false;
                btnZiel.setBackgroundColor(Color.parseColor(getString(R.color.Start_Ziel_pressed)));
                Toast.makeText(getApplicationContext(), "Zielpunkt auf der Karte wählen!", Toast.LENGTH_LONG).show();

                // Long Click für Koordinaten
                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

                    @Override
                    public void onMapClick(LatLng target) {
                        if (ziel_input == false) {
                            //Toast.makeText(getApplicationContext(), target.toString(), Toast.LENGTH_SHORT).show();
                            //  String message = edt_start.getText().toString();

                            Log.i("Zielpunkt", String.valueOf(target));

                            // Übergabe an startPos
                            OrsRequest.targetPos = target;

                            // Ausgabe der Koordinaten für Textfeld
                            TextView textView = findViewById(R.id.txt_target);
                            textView.setText("Latitude: " + String.valueOf(Math.round(OrsRequest.targetPos.latitude * 100000) / 100000.0) + "  Longitude: " + String.valueOf(Math.round(OrsRequest.targetPos.longitude * 100000) / 100000.0));

                            if (OrsRequest.targetPos != null) {
                                btnZiel.setBackgroundColor(Color.parseColor(getString(R.color.Ziel_Button)));
                                ziel_input = true;
                                markerZiel = googleMap.addMarker(new MarkerOptions().position(OrsRequest.targetPos).title("Ziel"));
                            }
                        }
                    }
                });
              }
        });


        // Klick auf Walk Button
        btnWalk.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View view) {
                if(walk_pressed == false){
                    car_pressed = false;
                    bike_pressed = false;
                    walk_pressed = true;
                Log.i("Button Walk", "Button Walk gedrückt");
                //btnBike.setBackgroundColor(Color.parseColor(getString(R.color.white)));
                btnWalk.setBackgroundColor(Color.parseColor(getString(R.color.Button_pressed)));
                //btnCar.setBackgroundColor(Color.parseColor(getString(R.color.white)));


                btnBike.setBackgroundColor(Color.parseColor(getString(R.color.Button_grey)));
                btnCar.setBackgroundColor(Color.parseColor(getString(R.color.Button_grey)));

                removeLayer();

                layerWalk.addLayerToMap();
            }

                else if(walk_pressed == true) {
                    car_pressed = false;
                    bike_pressed = false;
                    walk_pressed = false;
                removeLayer();
                bestLayerCar.addLayerToMap();
                bestLayerBike.addLayerToMap();
                bestLayerWalk.addLayerToMap();

                btnCar.setBackgroundColor(Color.parseColor(getString(R.color.Button_white)));
                btnBike.setBackgroundColor(Color.parseColor(getString(R.color.Button_white)));
                btnWalk.setBackgroundColor(Color.parseColor(getString(R.color.Button_white)));

            }
            }
        });

        // Klick auf Bike Button
        btnBike.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View view) {
                if(bike_pressed == false){
                    car_pressed = false;
                    bike_pressed = true;
                    walk_pressed = false;
                Log.i("Button Bike", "Button Bike gedrückt");
                btnBike.setBackgroundColor(Color.parseColor(getString(R.color.Button_pressed)));
                //btnWalk.setBackgroundColor(Color.parseColor(getString(R.color.white)));
                //btnCar.setBackgroundColor(Color.parseColor(getString(R.color.white)));


                btnWalk.setBackgroundColor(Color.parseColor(getString(R.color.Button_grey)));
                btnCar.setBackgroundColor(Color.parseColor(getString(R.color.Button_grey)));

                removeLayer();

                layerBike.addLayerToMap();
            }

                else if(bike_pressed == true) {
                    car_pressed = false;
                    bike_pressed = false;
                    walk_pressed = false;
                removeLayer();
                bestLayerCar.addLayerToMap();
                bestLayerBike.addLayerToMap();
                bestLayerWalk.addLayerToMap();

                btnCar.setBackgroundColor(Color.parseColor(getString(R.color.Button_white)));
                btnBike.setBackgroundColor(Color.parseColor(getString(R.color.Button_white)));
                btnWalk.setBackgroundColor(Color.parseColor(getString(R.color.Button_white)));

            }

            }
        });

        // Klick auf Car Button
        btnCar.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View view) {
                if(car_pressed == false) {
                    car_pressed = true;
                    bike_pressed = false;
                    walk_pressed = false;
                    Log.i("Button Car", "Button Car gedrückt");
                    //btnBike.setBackgroundColor(Color.parseColor(getString(R.color.white)));
                    //btnWalk.setBackgroundColor(Color.parseColor(getString(R.color.white)));
                    btnCar.setBackgroundColor(Color.parseColor(getString(R.color.Button_pressed)));


                    btnBike.setBackgroundColor(Color.parseColor(getString(R.color.Button_grey)));
                    btnWalk.setBackgroundColor(Color.parseColor(getString(R.color.Button_grey)));

                    removeLayer();

                    layerCar.addLayerToMap();
                }

                else if(car_pressed == true) {
                    car_pressed = false;
                    bike_pressed = false;
                    walk_pressed = false;
                    removeLayer();
                    bestLayerCar.addLayerToMap();
                    bestLayerBike.addLayerToMap();
                    bestLayerWalk.addLayerToMap();

                    btnCar.setBackgroundColor(Color.parseColor(getString(R.color.Button_white)));
                    btnBike.setBackgroundColor(Color.parseColor(getString(R.color.Button_white)));
                    btnWalk.setBackgroundColor(Color.parseColor(getString(R.color.Button_white)));

                }
            }
        });


        // Klick auf Button suche Starten
        MainActivity this2 = this;
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View view) {

                TextView textView1 = findViewById(R.id.txt_fahrrad);
                textView1.setText("Info Fahrrad");
                TextView textView2 = findViewById(R.id.txt_walk);
                textView2.setText("Info Fußgänger");
                TextView textView3 = findViewById(R.id.txt_car);
                textView3.setText("Info Auto");
                btnCar.setBackgroundColor(Color.parseColor(getString(R.color.Button_white)));
                btnBike.setBackgroundColor(Color.parseColor(getString(R.color.Button_white)));
                btnWalk.setBackgroundColor(Color.parseColor(getString(R.color.Button_white)));

                removeLayer();

                if (OrsRequest.startPos != null && OrsRequest.targetPos != null) {

                    Log.i("Startpunkt_Suche", String.valueOf(start));
                    Log.i("Zielpunkt_Suche", String.valueOf(target));

                    Log.i("Startpunkt_Suche", String.valueOf(OrsRequest.startPos));
                    Log.i("Zielpunkt_Suche", String.valueOf(OrsRequest.targetPos));


                    // Möglicherweise ist hier nicht alles richtig eingetragen
                    new OrsRequest(profilCar, OrsRequest.startPos, OrsRequest.targetPos, this2);
                    new OrsRequest(profilWalking, OrsRequest.startPos, OrsRequest.targetPos, this2);
                    new OrsRequest(profilBike, OrsRequest.startPos, OrsRequest.targetPos, this2);

                    // creating progress bar dialog
                    progressBar = new ProgressDialog(view.getContext());
                    progressBar.setCancelable(true);
                    progressBar.setMessage("Route wird berechnet ...");
                    progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressBar.setProgress(0);
                    progressBar.setMax(100);
                    progressBar.show();

                    //reset progress bar and filesize status
                    progressBarStatus = 0;
                    fileSize = 0;

                    new Thread(new Runnable() {
                        public void run() {
                            while (progressBarStatus < 100) {
                                // performing operation
                                progressBarStatus = doOperation();
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                // Updating the progress bar
                                progressBarHandler.post(new Runnable() {
                                    public void run() {
                                        progressBar.setProgress(progressBarStatus);
                                    }
                                });
                            }
                            // performing operation if file is downloaded,
                            if (progressBarStatus >= 100) {
                                // sleeping for 1 second after operation completed
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                // close the progress bar dialog
                                progressBar.dismiss();
                            }
                        }
                    }).start();



                }


            else {
                Log.i("Else", "Start und Ziel Koordinaten wählen");
                    Toast.makeText(getApplicationContext(), "Start und Ziel Koordinaten wählen", Toast.LENGTH_SHORT).show();

                // Hier ein Pop-Up Window oder Ähnliches einfügen!
            }

        }

        });
    }








    @Override
    public void processOrsResult(String profile, JSONObject response) {
        Log.i("processOrsResult", response.toString());





        GeoJsonLayer layer = new GeoJsonLayer(googleMap, response);

        switch (profile) {
            case "driving-car":
                layerCar = layer;

                break;

            case "cycling-road":
                layerBike = layer;

                break;

            case "foot-walking":
                layerWalk = layer;

                break;
            default:
                Log.d("Error:","Profil " + profile + " nicht wählbar");
                break;

        }


        //layer.addLayerToMap();

        double shortest_distance = 0;
        double duration_shortest_distance = 0;
        GeoJsonFeature shortest_feature = null;




        for(GeoJsonFeature feature : layer.getFeatures()){
                if (feature.hasProperty("summary")) {
                    String summary = feature.getProperty("summary").toString();

                    double distance;
                    double duration;

                    try {
                        JSONObject jsonSummary = new JSONObject(summary);

                        distance = jsonSummary.getDouble("distance");
                        duration = jsonSummary.getDouble("duration");

                        Log.d("Distance " + profile, String.valueOf(distance));
                        Log.d("Dauer " + profile, String.valueOf(duration));


                        if(shortest_feature == null || distance < shortest_distance){
                            shortest_distance = distance;
                            shortest_feature = feature;
                            duration_shortest_distance = duration;
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Log.d("Summary " + profile, summary);
                    Log.d("Shortest Distance " + profile, String.valueOf(shortest_distance));
                }
        }


        //layer.addLayerToMap();



        if(profile == profilCar) {
            TextView textView = findViewById(R.id.txt_car);
            textView.setText("Info Auto\n\n" + "Länge: " + String.valueOf(Math.round(shortest_distance/100.0)/10.0) + " km\n\n" + "Dauer: " + String.valueOf(Math.round(duration_shortest_distance/6.0)/10.0) + " min");



            bestLayerCar = new GeoJsonLayer(googleMap, new JSONObject());
            bestLayerCar.addFeature(shortest_feature);

            GeoJsonLineStringStyle lineStringStyle = new GeoJsonLineStringStyle();
            lineStringStyle.setColor(getResources().getColor(R.color.Auto));
            shortest_feature.setLineStringStyle(lineStringStyle);

            Log.d("best Layer Car", String.valueOf(bestLayerCar.isLayerOnMap()));
            bestLayerCar.addLayerToMap();
            Log.d("best Layer Car", String.valueOf(bestLayerCar.isLayerOnMap()));
        }

        else if(profile == profilBike) {
            TextView textView = findViewById(R.id.txt_fahrrad);
            textView.setText("Info Fahrrad\n\n" + "Länge: " + String.valueOf(Math.round(shortest_distance/100.0)/10.0) + " km\n\n" + "Dauer: " + String.valueOf(Math.round(duration_shortest_distance/6.0)/10.0) + " min");



            bestLayerBike = new GeoJsonLayer(googleMap, new JSONObject());
            bestLayerBike.addFeature(shortest_feature);

            GeoJsonLineStringStyle lineStringStyle = new GeoJsonLineStringStyle();
            lineStringStyle.setColor(getResources().getColor(R.color.Fahrrad));
            shortest_feature.setLineStringStyle(lineStringStyle);

            Log.d("best Layer Bike", String.valueOf(bestLayerBike));
            bestLayerBike.addLayerToMap();
        }

        else if(profile == profilWalking) {
            TextView textView = findViewById(R.id.txt_walk);
            textView.setText("Info Fußgänger\n\n" + "Länge: " + String.valueOf(Math.round(shortest_distance/100.0)/10.0) + " km\n\n" + "Dauer: " + String.valueOf(Math.round(duration_shortest_distance/6.0)/10.0) + " min");



            bestLayerWalk = new GeoJsonLayer(googleMap, new JSONObject());
            bestLayerWalk.addFeature(shortest_feature);

            GeoJsonLineStringStyle lineStringStyle = new GeoJsonLineStringStyle();
            lineStringStyle.setColor(getResources().getColor(R.color.Fuß));
            shortest_feature.setLineStringStyle(lineStringStyle);

            Log.d("best Layer Walk", String.valueOf(bestLayerWalk));
            bestLayerWalk.addLayerToMap();
        }




    }

    @Override
    public void processOrsError(String profile, VolleyError error) {
        Log.i("processOrsError", error.toString());

        String toastError = "Unbekannter Fehler";

        try {

            String errorContent = new String(error.networkResponse.data, "utf-8");
            Log.i("processOrsError", errorContent);

            JSONObject errorJSON = new JSONObject(errorContent);

            JSONObject errorJSON_temp = errorJSON.getJSONObject("error");
            toastError = errorJSON_temp.getString("code");

            Log.i("Toast Error Code", toastError);

            switch(toastError) {
                case "2004":
                    Toast.makeText(getApplicationContext(), "Start- und Zielpunkt dürfen maximal 150 km voneinander entfernt sein!", Toast.LENGTH_SHORT).show();
                    break;
               /* case "1000":

                    break;*/
                default:
                    Toast.makeText(getApplicationContext(), toastError, Toast.LENGTH_SHORT).show();
                    break;
            }


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Context appContext() {
        return this.getApplicationContext();
    }


    // checking how much file is downloaded and updating the file size
    public int doOperation() {
        //The range of ProgressDialog starts from 0 to 10000
        while (fileSize <= 10000) {
            fileSize++;
            if (fileSize == 1000) {
                return 10;
            } else if (fileSize == 2000) {
                return 20;
            } else if (fileSize == 3000) {
                return 30;
            } else if (fileSize == 4000) {
                return 40; // you can add more else if
            }
        }//end of while
        return 100;
    }//end of doOperation


public void removeLayer(){
    if(bestLayerCar != null) {
        bestLayerCar.removeLayerFromMap();
    }
    if(bestLayerBike != null) {
        bestLayerBike.removeLayerFromMap();
    }
    if(bestLayerWalk != null) {
        bestLayerWalk.removeLayerFromMap();
    }
    if(layerCar != null) {
        layerCar.removeLayerFromMap();
    }
    if(layerBike != null) {
        layerBike.removeLayerFromMap();
    }
    if(layerWalk != null) {
        layerWalk.removeLayerFromMap();
    }
}

}