package com.mabook.android.cyclo.core;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;

/**
 * Created by sng2c on 2014. 9. 17..
 */
public class CycloManager {
    public static final String ACTION_CONTROL = "com.mabook.android.cyclo.core.CycloService.ACTION_CONTROL";
    public static final String ACTION_BROADCAST = "com.mabook.android.cyclo.core.CycloService.ACTION_BROADCAST";
    public static final String AUTHORITY = "com.mabook.android.cyclo.provider";

    public static final String SESSION_FIELD_ID = "_id";
    public static final String SESSION_FIELD_PACKAGE_NAME = "package_name";
    public static final String SESSION_FIELD_APP_NAME = "app_name";
    public static final String SESSION_FIELD_SESSION_NAME = "session_name";
    public static final String SESSION_FIELD_START_TIME = "start_time";
    public static final String SESSION_FIELD_END_TIME = "end_time";
    public static final String[] SESSION_FIELD_ALL = new String[]{
            SESSION_FIELD_ID,
            SESSION_FIELD_PACKAGE_NAME,
            SESSION_FIELD_APP_NAME,
            SESSION_FIELD_SESSION_NAME,
            SESSION_FIELD_START_TIME,
            SESSION_FIELD_END_TIME
    };

    public static final String TRACK_FIELD_ID = "_id";
    public static final String TRACK_FIELD_SESSION_ID = "session_id";
    public static final String TRACK_FIELD_REGTIME = "regtime";
    public static final String TRACK_FIELD_ACCURACY = "acc";
    public static final String TRACK_FIELD_LATITUDE = "lat";
    public static final String TRACK_FIELD_LONGITUDE = "lng";
    public static final String TRACK_FIELD_ALTITUDE = "alt";
    public static final String TRACK_FIELD_SPEED = "speed";
    public static final String[] TRACK_FIELD_ALL = new String[]{
            TRACK_FIELD_ID,
            TRACK_FIELD_SESSION_ID,
            TRACK_FIELD_REGTIME,
            TRACK_FIELD_ACCURACY,
            TRACK_FIELD_LATITUDE,
            TRACK_FIELD_LONGITUDE,
            TRACK_FIELD_ALTITUDE,
            TRACK_FIELD_SPEED
    };

    public static final String KEY_BROADCAST_ACTION = "KEY_BROADCAST_ACTION";
    public static final String KEY_BROADCAST_TYPE = "KEY_BROADCAST_TYPE";
    public static final String KEY_BROADCAST_SESSION = "KEY_BROADCAST_SESSION";
    public static final String KEY_BROADCAST_TRACK = "KEY_BROADCAST_TRACK";
    public static final String KEY_BROADCAST_LOCATION = "KEY_BROADCAST_LOCATION";

    public static final String KEY_REQUEST_CODE = "KEY_REQUEST_CODE";
    public static final String KEY_RESULT = "KEY_RESULT";
    public static final String KEY_STATE = "KEY_STATE";
    public static final String KEY_PACKAGE_NAME = "KEY_PACKAGE_NAME";
    public static final String KEY_APP_NAME = "KEY_APP_NAME";
    public static final String KEY_PROFILE = "KEY_PROFILE";
    public static final String KEY_RECEIVER = "KEY_RECEIVER";

    public static final int CONTROL_NOT_DEFINED = 0;
    public static final int CONTROL_REQUEST = 1;
    public static final int CONTROL_RELEASE = 2;
    public static final int CONTROL_START = 3;
    public static final int CONTROL_STOP = 4;
    public static final int CONTROL_PAUSE = 5;
    public static final int CONTROL_RESUME = 6;
    public static final int CONTROL_UPDATE_PROFILE = 7;

    public static final int STATE_STOPPED = 1;
    public static final int STATE_STARTED = 2;
    public static final int STATE_PAUSED = 3;
    public static final int RESULT_NO = 0;
    public static final int RESULT_OK = 1;
    private static final String TAG = "CycloManager";
    private final Context mContext;
    private final Bundle mBaseBundle;

    public CycloManager(Context context, ResultReceiver resultReceiver) {
        mContext = context.getApplicationContext();
        mBaseBundle = new Bundle();
        mBaseBundle.putString(KEY_PACKAGE_NAME, mContext.getPackageName());
        mBaseBundle.putString(KEY_APP_NAME, mContext.getString(mContext.getApplicationInfo().labelRes));
        mBaseBundle.putParcelable(KEY_RECEIVER, resultReceiver);
    }

    public static String getControlCodeString(int controlCode) {
        switch (controlCode) {
            case CONTROL_NOT_DEFINED:
                return "CONTROL_NOT_DEFINED";
            case CONTROL_REQUEST:
                return "CONTROL_REQUEST";
            case CONTROL_RELEASE:
                return "CONTROL_RELEASE";
            case CONTROL_START:
                return "CONTROL_START";
            case CONTROL_STOP:
                return "CONTROL_STOP";
            case CONTROL_PAUSE:
                return "CONTROL_PAUSE";
            case CONTROL_RESUME:
                return "CONTROL_RESUME";
            case CONTROL_UPDATE_PROFILE:
                return "CONTROL_UPDATE_PROFILE";
            default:
                return "CONTROL_NOT_DEFINED";
        }
    }

    public static String getStateCodeString(int stateCode) {
        switch (stateCode) {
            case STATE_STOPPED:
                return "STATE_STOPPED";
            case STATE_STARTED:
                return "STATE_STARTED";
            case STATE_PAUSED:
                return "STATE_PAUSED";
            default:
                return "STATE_STOPPED";
        }
    }

    public static String getResultCodeString(int resultCode) {
        switch (resultCode) {
            case RESULT_NO:
                return "RESULT_NO";
            case RESULT_OK:
                return "RESULT_OK";
            default:
                return "RESULT_NO";
        }
    }

    public static String dumpLocation(Location loc, Location lastLocation) {
        if (loc == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("ACC:").append(loc.getAccuracy()).append("m").append("\n")
                .append("LAT:").append(loc.getLatitude()).append("\n")
                .append("LNG:").append(loc.getLongitude()).append("\n")
                .append("ALT:").append(loc.getAltitude()).append("m\n")
                .append("SPD:").append(loc.getSpeed()).append("m/s").append("\n");
        if (lastLocation != null) {
            sb.append("DIS:").append(loc.distanceTo(lastLocation)).append("m");
        }
        return sb.toString();
    }

    private Intent getControlIntent(int controlId) {
        Bundle b = new Bundle(mBaseBundle);
        Intent intent = new Intent(ACTION_CONTROL);
        intent.putExtras(b);
        intent.putExtra(KEY_REQUEST_CODE, controlId);
        return intent;
    }

    public void requestControl() {
        Intent intent = getControlIntent(CONTROL_REQUEST);
        mContext.startService(intent);
    }

//    public void releaseControl(){
//        Intent intent = getControlIntent(CONTROL_RELEASE);
//        mContext.startService(intent);
//    }

    public void start(CycloProfile profile, String broadcastAction) {
        Intent intent = getControlIntent(CONTROL_START);
        intent.putExtra(KEY_PROFILE, profile);
        intent.putExtra(KEY_BROADCAST_ACTION, broadcastAction);
        mContext.startService(intent);
    }

    public void updateProfile(CycloProfile profile) {
        Intent intent = getControlIntent(CONTROL_UPDATE_PROFILE);
        intent.putExtra(KEY_PROFILE, profile);
        mContext.startService(intent);
    }

    public void stop() {
        Intent intent = getControlIntent(CONTROL_STOP);
        mContext.startService(intent);
    }

    public void pause() {
        Intent intent = getControlIntent(CONTROL_PAUSE);
        mContext.startService(intent);
    }

    public void resume() {
        Intent intent = getControlIntent(CONTROL_RESUME);
        mContext.startService(intent);
    }



}
