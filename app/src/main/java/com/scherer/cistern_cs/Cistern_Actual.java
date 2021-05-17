package com.scherer.cistern_cs;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Cistern_Actual extends AppCompatActivity {

    static String SERVER_IP = "cs-host-hoes.spdns.de";
    static int SERVER_PORT  = 5050;

    ImageView cistern_image;
    TextView cistern_water_text;
    TextView cistern_temp_text;
    int server_response;
    float water_lever;
    int water_lever_int;
    float water_temp;
    float water_liter;
    Button button_now;
    Button button_historie;
    ViewGroup skala_feld;
    int y_view_size;
    Intent histroy_intent;
    ProgressBar progressBarWater;
    ProgressBar progressBarAir;
    ProgressBar progressBarAirNeg;
    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cistern__actual);

        webView = (WebView) findViewById(R.id.web_view);
        cistern_water_text = (TextView)findViewById(R.id.water);
        cistern_temp_text = (TextView)findViewById(R.id.temperatur);
        button_now         = (Button)findViewById(R.id.button_now);
        button_historie    = (Button)findViewById(R.id.button_history);
        cistern_image      = (ImageView)findViewById(R.id.imageView);
        progressBarWater   = (ProgressBar) findViewById(R.id.progressBarWater);
        progressBarAir     = (ProgressBar) findViewById(R.id.progressBarTemp);
        progressBarAirNeg  = (ProgressBar) findViewById(R.id.progressBarTempNeg);

        WebSettings webSettings = webView.getSettings();
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setAppCacheEnabled(true);
        webSettings.setAppCacheMaxSize(100 * 1000 * 1000);

        histroy_intent = new Intent(this, Cistern_history.class);

        /* action button "now" */
        button_now.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(internetAvailable()) {
                    new UploadImageAsyncTask().execute();
                }else {
                    Toast.makeText(getApplicationContext(), "Kein Internet vorhanden", Toast.LENGTH_LONG).show();
                }
            }
        });

        /* action button "now" */
        button_historie.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivity(histroy_intent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        try {
            if (internetAvailable()) {
                new UploadImageAsyncTask().execute();
            } else {
                Toast.makeText(getApplicationContext(), "Kein Internet vorhanden", Toast.LENGTH_LONG).show();
            }
        }catch (Exception e){
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.d("Meine App", "Fehler beim empfgang...");
        }

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

        });
        webView.loadUrl("http://cs-host-hoes.spdns.de:3000/d-solo/Kad-zTzgz/waterlevel-and-temperature-today?panelId=2&orgId=1&");
    }

    /* a new task for upload the photo */
    public class UploadImageAsyncTask extends AsyncTask {
        /* handles everthing in the async task */
        @Override
        protected Object doInBackground(Object[] params) {
            server_response = 1;
            try {
                Log.d("Meine App", "Starte Verbindung");
			    /* conncet to server */
                Socket socket_client = new Socket(SERVER_IP, SERVER_PORT);
                Log.d("Meine App", "Start connection to " + SERVER_IP + " on Port " + Integer.toString(SERVER_PORT));

			    /* create input- and outputstream */
                DataOutputStream socket_output = new DataOutputStream(socket_client.getOutputStream());
                DataInputStream socket_input = new DataInputStream(socket_client.getInputStream());

			    /* send  one byte to start arduino */
                socket_output.writeByte(1);
                Log.d("Meine App", "Send a byte");

			    /* received water level - water temperature - water in liter */
                water_lever_int = socket_input.readShort();
                water_lever = ((float)(water_lever_int)) / 100;
                water_lever = (float)((int)(water_lever * 100 + 0.5))/100;
                water_temp  = (((float)(socket_input.readShort())) / 10) - 50;
                water_temp = (float)((int)(water_temp * 10 + 0.5))/10;
                water_liter = (float)(socket_input.readShort());
                water_liter = (float)((int)(water_liter * 10 + 0.5))/10;
                Log.d("Meine App", "reveived data");

                socket_output.close();
                Log.d("Meine App", "Close Socket ...");

                server_response = 2;


            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Log.d("Meine App", "Fehler beim empfgang...");
            }
            return null;
        }

        @Override
        /* starts after asyc task are finished */
        protected void onPostExecute(Object o){
            try {
                Log.d("Meine App","Starte Anzeige");
                if (server_response == 2) {
                    if ( (water_lever_int % 10) == 0){
                        cistern_temp_text.setText("Temperatur: "  + Float.toString(water_temp) + "°C");
                        cistern_water_text.setText("Füllstand: " + Float.toString(water_lever) + "0 m /" + Float.toString(water_liter) + "l");
                        progressBarWater.setProgress((int)(water_lever*1000));

                        if((int)(water_temp) >= 0) {
                            progressBarAir.setProgress((int) (water_temp * 10));
                            progressBarAirNeg.setProgress((int) (0));
                        }else{
                            progressBarAir.setProgress(0);
                            progressBarAirNeg.setProgress((int) (-water_temp * 10));
                        }
                    }else {
                        cistern_temp_text.setText("Temperatur: "  + Float.toString(water_temp) + "°C");
                        cistern_water_text.setText("Füllstand: " + Float.toString(water_lever) + " m /" + Float.toString(water_liter) + "l");
                        progressBarWater.setProgress((int)(water_lever*1000));

                        progressBarAir.setProgress((int) (10));
                        if((int)(water_temp) >= 0) {
                            progressBarAir.setProgress((int) (water_temp * 10));
                            progressBarAirNeg.setProgress((int) (0));
                        }else{
                            progressBarAir.setProgress(0);
                            progressBarAirNeg.setProgress((int) (-water_temp * 10));
                        }
                    }
                    if(water_lever > 1.3) {
                        cistern_image.setImageResource(R.drawable.regentonne_voll);
                    }else if(water_lever > 1.10){
                        cistern_image.setImageResource(R.drawable.regentonne_130);
                    }else if(water_lever > 0.95){
                        cistern_image.setImageResource(R.drawable.regentonne_110);
                    }else if(water_lever > 0.80){
                        cistern_image.setImageResource(R.drawable.regentonne_95);
                    }else if(water_lever > 0.65){
                        cistern_image.setImageResource(R.drawable.regentonne_80);
                    }else if(water_lever > 0.50){
                        cistern_image.setImageResource(R.drawable.regentonne_65);
                    }else if(water_lever > 0.35){
                        cistern_image.setImageResource(R.drawable.regentonne_50);
                    }else if(water_lever > 0.17){
                        cistern_image.setImageResource(R.drawable.regentonne_35);
                    }else if(water_lever > 0.02){
                        cistern_image.setImageResource(R.drawable.regentonne_17);
                    }else {
                        cistern_image.setImageResource(R.drawable.regentonne_0);
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Fehler bei empfang", Toast.LENGTH_LONG).show();
                }
                super.onPostExecute(o);

            } catch (Exception e) {
                e.printStackTrace();
                Log.d("Meine App","Fehler beim Anzeigen");
            }
        }
    }

    private boolean internetAvailable(){
        try{
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Meine App","Fehler beim Internetcheck");
            return false;
        }
    }
}
