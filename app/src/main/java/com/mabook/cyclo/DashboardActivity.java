package com.mabook.cyclo;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.mabook.cyclo.core.GPSConnector;


public class DashboardActivity extends Activity {

    private Button buttonStart;
    private Button buttonStop;
    private GPSConnector gpsConn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        buttonStart = (Button) findViewById(R.id.button_start);
        buttonStop = (Button) findViewById(R.id.button_stop);
        gpsConn = new GPSConnector(this, new GPSConnector.StatusListener(){
            @Override
            public void onReceiveStatus(Bundle bundle) {
                boolean started = bundle.getBoolean("started", false);
                buttonStart.setEnabled(!started);
                buttonStop.setEnabled(started);
            }

            @Override
            public void onReceiveUpdate(Bundle bundle) {
                Toast.makeText(DashboardActivity.this,"UPDATE:"+bundle.getString("type"),Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
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

    @Override
    protected void onStart() {
        super.onStart();
        gpsConn.bindService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        gpsConn.unbindService();
    }

    public void onClickStart(View view){
        gpsConn.start("DashBoard");
    }
    public void onClickStop(View view){
        gpsConn.stop();
    }
}
