package com.mabook.android.cyclo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.mabook.android.cyclo.core.CycloManager;
import com.mabook.android.cyclo.core.CycloProfile;


public class DashboardActivity extends Activity {

    private static final String TAG = "DashboardActivity";
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            String type = extras.getString(CycloManager.KEY_BROADCAST_TYPE);
            long sessionId = extras.getLong(CycloManager.KEY_BROADCAST_SESSION);
            long trackId = extras.getLong(CycloManager.KEY_BROADCAST_TRACK);
            Location location = extras.getParcelable(CycloManager.KEY_BROADCAST_LOCATION);

            textUpdate.setText(type + "\nsessionId:" + sessionId + "\ntrackId:" + trackId + "\nlocation:" + CycloManager.dumpLocation(location, null));
        }
    };
    private Button buttonStart;
    private Button buttonStop;
    private Button buttonPause;
    private Button buttonResume;
    private Button buttonUpdate;
    private TextView textUpdate;
    private CycloManager cycloManager;
    private Spinner options;

    private void updateButtons(int state) {
        switch (state) {
            case CycloManager.STATE_STOPPED:
                buttonStart.setEnabled(true);
                buttonStop.setEnabled(false);
                buttonResume.setEnabled(false);
                buttonPause.setEnabled(false);
                options.setEnabled(true);
                buttonUpdate.setEnabled(false);
                break;
            case CycloManager.STATE_STARTED:
                buttonStart.setEnabled(false);
                buttonStop.setEnabled(true);
                buttonResume.setEnabled(false);
                buttonPause.setEnabled(true);
                options.setEnabled(true);
                buttonUpdate.setEnabled(true);
                break;
            case CycloManager.STATE_PAUSED:
                buttonStart.setEnabled(false);
                buttonStop.setEnabled(true);
                buttonResume.setEnabled(true);
                buttonPause.setEnabled(false);
                options.setEnabled(false);
                buttonUpdate.setEnabled(false);
                break;
            default:
                buttonStart.setEnabled(false);
                buttonStop.setEnabled(false);
                buttonResume.setEnabled(false);
                buttonPause.setEnabled(false);
                options.setEnabled(false);
                buttonUpdate.setEnabled(false);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        buttonStart = (Button) findViewById(R.id.button_start);
        buttonStop = (Button) findViewById(R.id.button_stop);
        buttonPause = (Button) findViewById(R.id.button_pause);
        buttonResume = (Button) findViewById(R.id.button_resume);
        textUpdate = (TextView) findViewById(R.id.update);
        options = (Spinner) findViewById(R.id.options);
        buttonUpdate = (Button) findViewById(R.id.button_update);


        cycloManager = new CycloManager(this, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                int result = resultData.getInt(CycloManager.KEY_RESULT);
                int state = resultData.getInt(CycloManager.KEY_STATE);

                switch (resultCode) {
                    case CycloManager.CONTROL_REQUEST:
                        if (result == CycloManager.RESULT_OK) {
                            Log.d(TAG, "SUCCESS TO CONTROL");
                        } else {
                            Log.d(TAG, "FAIL TO CONTROL");
                        }
                        break;
                }

                if (result == CycloManager.RESULT_OK) {
                    updateButtons(state);
                } else {
                    updateButtons(0);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        cycloManager.requestControl();
        registerReceiver(receiver, new IntentFilter(CycloManager.ACTION_BROADCAST));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(receiver);
    }

    public CycloProfile getProfile(int idx) {
        CycloProfile profile = null;
        Criteria criteria = null;
        switch (idx) {
            case 0:
                break;
            case 1:
                criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                criteria.setPowerRequirement(Criteria.POWER_HIGH);
                criteria.setAltitudeRequired(true);
                criteria.setBearingRequired(false);
                criteria.setSpeedRequired(true);
                criteria.setCostAllowed(true);
                profile = new CycloProfile();
                profile.setCriteria(criteria);
                profile.setMinTime(0);
                profile.setMinDistance(1);
                break;
            case 2:
                criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_COARSE);
                criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
                criteria.setAltitudeRequired(true);
                criteria.setBearingRequired(false);
                criteria.setSpeedRequired(true);
                criteria.setCostAllowed(true);
                profile = new CycloProfile();
                profile.setCriteria(criteria);
                profile.setMinTime(0);
                profile.setMinDistance(0);
                break;
            case 3:
                criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                criteria.setPowerRequirement(Criteria.POWER_LOW);
                criteria.setAltitudeRequired(false);
                criteria.setBearingRequired(false);
                criteria.setSpeedRequired(true);
                criteria.setCostAllowed(true);
                profile = new CycloProfile();
                profile.setCriteria(criteria);
                profile.setMinTime(1000 * 30);
                profile.setMinDistance(1);
                break;
        }
        return profile;
    }

    public void onClickStart(View view) {
        int pos = options.getSelectedItemPosition();
        CycloProfile profile = getProfile(pos);
        cycloManager.start(profile, null);
    }

    public void onClickStop(View view) {
        cycloManager.stop();
    }

    public void onClickUpdate(View view) {
        int pos = options.getSelectedItemPosition();
        CycloProfile profile = getProfile(pos);
        cycloManager.updateProfile(profile);
    }


    public void onClickPause(View view) {
        cycloManager.pause();
    }

    public void onClickResume(View view) {
        cycloManager.resume();
    }
}
