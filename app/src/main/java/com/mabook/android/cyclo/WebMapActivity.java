package com.mabook.android.cyclo;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.mabook.android.cyclo.core.CycloManager;
import com.mabook.android.cyclo.core.data.CycloSession;


public class WebMapActivity extends Activity {

    private static final String TAG = "WebMapActivity";
    private WebView mWebView;
    private CycloSession mSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_map);

        Intent startIntent = getIntent();
        Bundle startBundle = startIntent.getExtras();
        if (startBundle != null) {
            mSession = startBundle.getParcelable("session");
        }

        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl("http://php.mabook.com/cyclo/map.html");
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                StringBuilder sb = new StringBuilder();

                Uri contentUri = Uri.parse("content://" + CycloManager.AUTHORITY + "/session/" + mSession.getId() + "/track");
                Cursor cursor = getContentResolver().query(contentUri, CycloManager.TRACK_FIELD_ALL, null, null, null);
//                ArrayList<CycloTrack> tracks = new ArrayList<CycloTrack>();


                while (cursor.moveToNext()) {
                    String lat = String.valueOf(cursor.getDouble(cursor.getColumnIndex(CycloManager.TRACK_FIELD_LATITUDE)));
                    String lng = String.valueOf(cursor.getDouble(cursor.getColumnIndex(CycloManager.TRACK_FIELD_LONGITUDE)));
                    sb.append("{")
                            .append("\"lat\":").append(lat).append(",")
                            .append("\"lng\":").append(lng)
                            .append("}\\n");
                }

                String script = "javascript:apply('" + sb.toString() + "');";
                view.loadUrl(script);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.web_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
