package com.mabook.cyclo.core;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.mabook.cyclo.R;

import static com.mabook.cyclo.core.CycloConnector.MSG_PAUSE;
import static com.mabook.cyclo.core.CycloConnector.MSG_RESUME;
import static com.mabook.cyclo.core.CycloConnector.MSG_START;
import static com.mabook.cyclo.core.CycloConnector.MSG_STATUS;
import static com.mabook.cyclo.core.CycloConnector.MSG_STOP;
import static com.mabook.cyclo.core.CycloConnector.MSG_UPDATE_PROFILE;
import static com.mabook.cyclo.core.CycloConnector.STATE_NOT_ALLOWED;
import static com.mabook.cyclo.core.CycloConnector.STATE_PAUSED;
import static com.mabook.cyclo.core.CycloConnector.STATE_STARTED;
import static com.mabook.cyclo.core.CycloConnector.STATE_STOPPED;


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
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    private final CycloProfile defaultProfile;
    public int state = STATE_STOPPED;
    Location lastLocation = null;
    boolean paused = false;
    LocationListener mDummyLocationListener = new LocationListener() {
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
    private String mBroadcastAction;
    private String mPackageName;
    private String mAction;
    private String mClassName;
    private String mStartedBy;
    private LocationManager mLocationManager;
    private CycloProfile mCurrentProfile;
    private boolean mRestarting = false;
    LocationListener mLocationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            if (mRestarting) {
                mLocationManager.removeUpdates(mDummyLocationListener);
                mRestarting = false;
            }
            if (state == STATE_PAUSED) { // PAUSED
                return;
            }
            if (location != null) {
                Log.d(TAG, "location:" + CycloConnector.dumpLocation(location, lastLocation));
                Bundle b = new Bundle();
                b.putParcelable("location", location);
                sendBroadcast("UPDATE", b);
                lastLocation = location;
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            Log.d(TAG, "provider:" + s + ", " + i);
            Log.d(TAG, "provider bundle:" + bundle.toString());
            sendBroadcast("UPDATE_PROVIDER!" + s, null);
        }

        @Override
        public void onProviderEnabled(String s) {
            Log.d(TAG, "provider+:" + s);
            sendBroadcast("UPDATE_PROVIDER+" + s, null);
        }

        @Override
        public void onProviderDisabled(String s) {
            Log.d(TAG, "provider-:" + s);
            sendBroadcast("UPDATE_PROVIDER-" + s, null);
        }
    };

    public CycloService() {
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
        defaultProfile.setMinDistance(0);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.addGpsStatusListener(mGpsListener);

    }

    void replyNotAllowed(Message msg) {
        if (msg.replyTo != null) {
            Message reply = new Message();
            reply.what = MSG_STATUS;
            Bundle b = new Bundle();
            b.putInt("state", STATE_NOT_ALLOWED);
            reply.setData(b);
            try {
                msg.replyTo.send(reply);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    void replyStatus(Message msg) {
        if (msg.replyTo != null) {
            Message reply = new Message();
            reply.what = MSG_STATUS;
            Bundle b = new Bundle();
            b.putInt("state", state);
            reply.setData(b);
            try {
                msg.replyTo.send(reply);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    void notifyForeground(String ticker, String text) {
        Intent actionIntent;
        if (mAction != null) {
            actionIntent = new Intent(mAction);
        } else if (mClassName != null) {
            actionIntent = new Intent();
            actionIntent.setComponent(new ComponentName(mPackageName, mClassName));
        } else {
            actionIntent = getPackageManager().getLaunchIntentForPackage(mPackageName);
        }
//        actionIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);

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
        broad.putExtra("type", type);
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
    }

    void safeRestartLocationListening() {
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mDummyLocationListener);
        mRestarting = true;
        stopLocationListening();
        startLocationListening("safeRestartLocationListening");
    }

    void startLog(Bundle data) {
        Log.d(TAG, "startLog");

        mAction = data.getString("action", null);
        mClassName = data.getString("className", null);
        mPackageName = data.getString("packageName", null);
        mStartedBy = data.getString("startedBy", null);
        mBroadcastAction = data.getString("broadcastAction", null);
        mCurrentProfile = data.getParcelable("profile");

        notifyForeground(getString(R.string.noti_ticker_start),
                String.format(getString(R.string.noti_text_started_by), mStartedBy));

        sendBroadcast("STARTED", null);

        startLocationListening("startLog");

        state = STATE_STARTED;
    }

    void updateProfile(Bundle data) {

        Log.d(TAG, "updateProfile");

        mCurrentProfile = data.getParcelable("profile");

        sendBroadcast("UPDATE_PROFILE", null);

        safeRestartLocationListening();

        state = STATE_STARTED;
    }

    void stopLog() {
        Log.d(TAG, "stopLog");

        stopForeground(true);

        sendBroadcast("STOPPED", null);

        stopLocationListening();

        state = STATE_STOPPED;

        mPackageName = null;
        lastLocation = null;
    }

    void pauseLog() {
        Log.d(TAG, "stopLog");

        notifyForeground(getString(R.string.noti_ticker_pause),
                String.format(getString(R.string.noti_text_paused_by), mStartedBy));

        sendBroadcast("PAUSED", null);

        state = STATE_PAUSED;
        lastLocation = null;
    }

    void resumeLog() {
        Log.d(TAG, "stopLog");

        notifyForeground(getString(R.string.noti_ticker_resume),
                String.format(getString(R.string.noti_text_started_by), mStartedBy));

        sendBroadcast("RESUMED", null);

        state = STATE_STARTED;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return mMessenger.getBinder();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        mLocationManager.removeGpsStatusListener(mGpsListener);
    }

    boolean checkStarter(Bundle data) {
        if (mPackageName == null) {
            return true;
        } else if (mPackageName.equals(data.getString("packageName"))) {
            return true;
        }
        return false;
    }

    class IncomingHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            if (checkStarter(msg.getData())) {
                switch (msg.what) {
                    case MSG_START:
                        startLog(msg.getData());
                        replyStatus(msg);
                        break;
                    case MSG_STOP:
                        stopLog();
                        replyStatus(msg);
                        break;
                    case MSG_PAUSE:
                        pauseLog();
                        replyStatus(msg);
                        break;
                    case MSG_RESUME:
                        resumeLog();
                        replyStatus(msg);
                        break;
                    case MSG_STATUS:
                        replyStatus(msg);
                        break;
                    case MSG_UPDATE_PROFILE:
                        updateProfile(msg.getData());
                        replyStatus(msg);
                        break;
                    default:
                        super.handleMessage(msg);
                }
            } else {
                switch (msg.what) {
                    case MSG_STATUS:
                        replyStatus(msg);
                        break;
                    default:
                        replyNotAllowed(msg);
                }
            }

        }
    }

}
