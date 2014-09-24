package com.mabook.cyclo.core;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;

/**
 * Created by sng2c on 2014. 9. 17..
 */
public class CycloManager {
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

    public static final String ACTION_CONTROL = "com.mabook.cyclo.core.CycloService.ACTION_CONTROL";

    public static final int STATE_STOPPED = 1;
    public static final int STATE_STARTED = 2;
    public static final int STATE_PAUSED = 3;
    public static final int STATE_NOT_ALLOWED = 4;

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

    public static String dumpLocation(Location loc, Location lastLocation) {
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

    public void start(CycloProfile profile) {
        Intent intent = getControlIntent(CONTROL_START);
        intent.putExtra(KEY_PROFILE, profile);
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
