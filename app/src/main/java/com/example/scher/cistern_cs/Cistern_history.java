package com.example.scher.cistern_cs;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class Cistern_history extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cistern_history);

        WebView webView = (WebView) findViewById(R.id.web_view);
        WebSettings webSettings = webView.getSettings();
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setAppCacheEnabled(true);
        webSettings.setAppCacheMaxSize(100 * 1000 * 1000);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

        });
        //webView.loadUrl("file:///android_asset/index.html"); //this is why you needed the assets folder
        //webView.loadUrl("http://cs-host-hoes.spdns.de:3000/d-solo/uX9fzlZgz/today?orgId=1&panelId=2&theme=light");
        webView.loadUrl("http://cs-host-hoes.spdns.de:3000/d-solo/Kad-zTzgz/waterlevel-and-temperature-today?panelId=2&orgId=1&");
        //webView.loadUrl("http://cs-host-hoes.spdns.de:3000/d/Kad-zTzgz/waterlevel-and-temperature-today?orgId=1");
    }
}
