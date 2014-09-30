package com.mabook.android.cyclo.core;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;

import com.mabook.android.cyclo.R;
import com.mabook.android.cyclo.core.data.CycloProfile;

public class CycloService extends Service {

    private static final String TAG = "CycloService";
    GpsStatus.Listener mGpsListener = new GpsStatus.Listener() {
        @Override
        public void onGpsStatusChanged(int i) {
            Log.d(TAG, "onGpsStatusChanged : " + i);
            String type = null;
            switch (i) {
                case GpsStatus.GPS_EVENT_STARTED:
                    type = "GPS_EVENT_STARTED";
                    break;
                case GpsStatus.GPS_EVENT_STOPPED:
                    type = "GPS_EVENT_STOPPED";
                    break;
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    type = "GPS_EVENT_FIRST_FIX";
                    break;
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    type = "GPS_EVENT_SATELLITE_STATUS";
                    break;
            }
            if (type != null)
                sendBroadcast(type, null);
        }
    };
    private final CycloProfile defaultProfile;
    public int mState = CycloManager.STATE_STOPPED;
    public long mLastTrackId = -1;
    private CycloDatabase mDatabase;
    private Bundle mCurrentControllerData;
    private Location lastLocation = null;
    private String mPackageName;
    private String mAppName;
    private LocationManager mLocationManager;
    private CycloProfile mCurrentProfile;
    private boolean mRestarting = false;
    private long mSessionId = -1;
    private String mBroadcastAction;


    private LocationListener mDummyLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String s) {
        }
    };
    private LocationListener mLocationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            if (mRestarting) {
                mLocationManager.removeUpdates(mDummyLocationListener);
                mRestarting = false;
            }
            if (mState == CycloManager.STATE_PAUSED) { // PAUSED
                return;
            }
            if (location != null) {
                if (mSessionId != -1) {
                    Log.d(TAG, "location:" + CycloManager.dumpLocation(location, lastLocation));

                    mLastTrackId = mDatabase.insertTrack(mSessionId, location.getAccuracy(), location.getLatitude(), location.getLongitude(), location.getAltitude(), location.getSpeed());

                    Bundle b = new Bundle();
                    b.putParcelable(CycloManager.KEY_BROADCAST_LOCATION, location);
                    sendBroadcast("UPDATE", b);
                }
                lastLocation = location;
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            Log.d(TAG, "provider:" + s + ", " + i);
            Log.d(TAG, "provider bundle:" + bundle.toString());
        }

        @Override
        public void onProviderEnabled(String s) {
            Log.d(TAG, "provider+:" + s);
        }

        @Override
        public void onProviderDisabled(String s) {
            Log.d(TAG, "provider-:" + s);
        }
    };

    public CycloService() {
        super();

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(true);
        criteria.setCostAllowed(true);

        defaultProfile = new CycloProfile();
        defaultProfile.setCriteria(criteria);
        defaultProfile.setMinTime(0);
        defaultProfile.setMinDistance(1);
    }


    private boolean canControl(String packageName) {
        if (mPackageName == null)
            return true;

        if (mState == CycloManager.STATE_STOPPED) {
            return true;
        }

        if (mPackageName.equals(packageName)) {
            return true;
        }

        return false;
    }

    private void sendResult(ResultReceiver resultReceiver, int requestCode, int resultCode) {
        if (resultReceiver != null) {
            Bundle d = new Bundle(mCurrentControllerData);
            d.putInt(CycloManager.KEY_STATE, mState);
            d.putInt(CycloManager.KEY_RESULT, resultCode);
            d.putLong(CycloManager.KEY_BROADCAST_SESSION, mSessionId);
            resultReceiver.send(requestCode, d);
        }
        long id = mDatabase.insertControlLog(mPackageName, mAppName, CycloManager.getControlCodeString(requestCode),
                CycloManager.getResultCodeString(resultCode));
        Log.d(TAG, "ControlLog inserted #" + id);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, intent.toString());

        if (mDatabase == null) {
            mDatabase = new CycloDatabase(getApplicationContext());
        }

        int requestCode = intent.getIntExtra(CycloManager.KEY_REQUEST_CODE, CycloManager.CONTROL_NOT_DEFINED);
        ResultReceiver resultReceiver = intent.getParcelableExtra(CycloManager.KEY_RECEIVER);

        String packageName = intent.getStringExtra(CycloManager.KEY_PACKAGE_NAME);
        if (canControl(packageName)) {
            switch (requestCode) {
                case CycloManager.CONTROL_REQUEST:
                    setControllerData(intent.getExtras());
                    sendResult(resultReceiver, requestCode, CycloManager.RESULT_OK);
                    break;
                case CycloManager.CONTROL_START:
                    doStart();
                    sendResult(resultReceiver, requestCode, CycloManager.RESULT_OK);
                    break;
                case CycloManager.CONTROL_STOP:
                    doStop();
                    sendResult(resultReceiver, requestCode, CycloManager.RESULT_OK);
                    break;
                case CycloManager.CONTROL_PAUSE:
                    doPause();
                    sendResult(resultReceiver, requestCode, CycloManager.RESULT_OK);
                    break;
                case CycloManager.CONTROL_RESUME:
                    doResume();
                    sendResult(resultReceiver, requestCode, CycloManager.RESULT_OK);
                    break;
                case CycloManager.CONTROL_UPDATE_PROFILE:
                    setControllerData(intent.getExtras());
                    doUpdateProfile();
                    sendResult(resultReceiver, requestCode, CycloManager.RESULT_OK);
                    break;
            }
        } else {
            if (resultReceiver != null)
                sendResult(resultReceiver, requestCode, CycloManager.RESULT_NO);
        }

        return Service.START_NOT_STICKY;
    }

    private void setControllerData(Bundle data) {
        mPackageName = data.getString(CycloManager.KEY_PACKAGE_NAME);
        mAppName = data.getString(CycloManager.KEY_APP_NAME);
        mCurrentProfile = data.getParcelable(CycloManager.KEY_PROFILE);

        String broadcast = data.getString(CycloManager.KEY_BROADCAST_ACTION);
        if (broadcast != null) {
            mBroadcastAction = broadcast;
        } else {
            mBroadcastAction = CycloManager.ACTION_BROADCAST;
        }

        data.remove(CycloManager.KEY_RECEIVER);
        data.remove(CycloManager.KEY_PROFILE);
        mCurrentControllerData = data;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.addGpsStatusListener(mGpsListener);

    }

    void notifyForeground(String ticker, String text) {
        Intent actionIntent;
        actionIntent = getPackageManager().getLaunchIntentForPackage(mPackageName);

        PendingIntent pintent = PendingIntent.getActivity(getApplicationContext(), 0, actionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        Notification notification = new Notification.Builder(getApplicationContext())
                .setContentIntent(pintent)
                .setContentTitle(getString(R.string.noti_title_cyclo))
                .setContentText(text)
                .setTicker(ticker)
                .setSmallIcon(R.drawable.ic_launcher)
                .setWhen(System.currentTimeMillis()).build();

        startForeground(1, notification);
    }

    void sendBroadcast(String type, Bundle b) {
        Intent broad = new Intent(mBroadcastAction);
        if (b != null) broad.putExtras(b);
        broad.putExtra(CycloManager.KEY_BROADCAST_TYPE, type);
        broad.putExtra(CycloManager.KEY_BROADCAST_SESSION, mSessionId);
        broad.putExtra(CycloManager.KEY_BROADCAST_TRACK, mLastTrackId);
        sendBroadcast(broad);
    }

    CycloProfile getCurrentProfile() {
        if (mCurrentProfile == null) {
            Log.d(TAG, "Using Default Profile");
            return defaultProfile;
        } else {
            Log.d(TAG, "Using Current Profile");
            return mCurrentProfile;
        }
    }

    String getBestProvider(String where) {
        String best = mLocationManager.getBestProvider(getCurrentProfile().getCriteria(), true);
        Log.d(TAG, "BestProvider from " + where + " : " + best);
        if (best == null) {
            best = LocationManager.NETWORK_PROVIDER;
        }
        return best;
    }

    String startLocationListening(String where) {
        Log.d(TAG, "startLocationListening from " + where);
        String bestProvider = getBestProvider(where);
        CycloProfile profile = getCurrentProfile();
        mLocationManager.requestLocationUpdates(bestProvider, profile.getMinTime(), profile.getMinDistance(), mLocationListener);
        return bestProvider;
    }

    void stopLocationListening() {
        mLocationManager.removeUpdates(mLocationListener);
        mLocationManager.removeGpsStatusListener(mGpsListener);
    }

    void safeRestartLocationListening() {
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mDummyLocationListener);
        mRestarting = true;
        stopLocationListening();
        startLocationListening("safeRestartLocationListening");
    }

    void doStart() {
        Log.d(TAG, "doStart");

        notifyForeground(getString(R.string.noti_ticker_start),
                String.format(getString(R.string.noti_text_started_by), mAppName));

        startLocationListening("doStart");
        mSessionId = mDatabase.insertSession(mPackageName, mAppName, null);

        mState = CycloManager.STATE_STARTED;
    }

    void doUpdateProfile() {

        Log.d(TAG, "doUpdateProfile");

        safeRestartLocationListening();

        mState = CycloManager.STATE_STARTED;
    }

    void doStop() {
        Log.d(TAG, "doStop");

        stopForeground(true);

        stopLocationListening();

        mDatabase.updateSessionStop(mSessionId);
        mSessionId = -1;
        mLastTrackId = -1;

        mState = CycloManager.STATE_STOPPED;


        lastLocation = null;
    }

    void doPause() {
        Log.d(TAG, "doPause");

        notifyForeground(getString(R.string.noti_ticker_pause),
                String.format(getString(R.string.noti_text_paused_by), mAppName));

        mState = CycloManager.STATE_PAUSED;
        lastLocation = null;
    }

    void doResume() {
        Log.d(TAG, "doResume");

        notifyForeground(getString(R.string.noti_ticker_resume),
                String.format(getString(R.string.noti_text_started_by), mAppName));

        mState = CycloManager.STATE_STARTED;
    }


    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        if (mDatabase != null)
            mDatabase.close();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
