package com.mabook.cyclo.core;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
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
import static com.mabook.cyclo.core.CycloConnector.STATE_NOT_ALLOWED;
import static com.mabook.cyclo.core.CycloConnector.STATE_PAUSED;
import static com.mabook.cyclo.core.CycloConnector.STATE_STARTED;
import static com.mabook.cyclo.core.CycloConnector.STATE_STOPPED;


public class CycloService extends Service {


    private static final String TAG = "CycloService";
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    public int state = STATE_STOPPED;
    private String mBroadcastAction;
    private String mPackageName;
    private String mAction;
    private String mClassName;
    private String mStartedBy;

    public CycloService() {
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

    void noti(String ticker, String text) {
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

    void startLog(Bundle data) {
        Log.d(TAG, "startLog");

        mAction = data.getString("action", null);
        mClassName = data.getString("className", null);
        mPackageName = data.getString("packageName", null);
        mStartedBy = data.getString("startedBy", null);
        mBroadcastAction = data.getString("broadcastAction", null);

        noti(getString(R.string.noti_ticker_start),
                String.format(getString(R.string.noti_text_started_by), mStartedBy));

        Intent broad = new Intent(mBroadcastAction);
        broad.putExtra("type", "STARTED");
        sendBroadcast(broad);

        state = STATE_STARTED;
    }

    boolean checkStarter(Bundle data) {
        if (mPackageName == null) {
            return true;
        } else if (mPackageName.equals(data.getString("packageName"))) {
            return true;
        }
        return false;
    }

    void stopLog() {
        Log.d(TAG, "stopLog");

        stopForeground(true);

        Intent broad = new Intent(mBroadcastAction);
        broad.putExtra("type", "STOPPED");
        sendBroadcast(broad);

        state = STATE_STOPPED;

        mPackageName = null;
    }

    void pauseLog() {
        Log.d(TAG, "stopLog");

        noti(getString(R.string.noti_ticker_pause),
                String.format(getString(R.string.noti_text_paused_by), mStartedBy));

        Intent broad = new Intent(mBroadcastAction);
        broad.putExtra("type", "PAUSED");
        sendBroadcast(broad);

        state = STATE_PAUSED;
    }

    void resumeLog() {
        Log.d(TAG, "stopLog");

        noti(getString(R.string.noti_ticker_resume),
                String.format(getString(R.string.noti_text_started_by), mStartedBy));

        Intent broad = new Intent(mBroadcastAction);
        broad.putExtra("type", "RESUMED");
        sendBroadcast(broad);

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
                    default:
                        super.handleMessage(msg);
                }
            } else {
                switch (msg.what) {
                    case MSG_STATUS:
                        replyStatus(msg);
                    default:
                        replyNotAllowed(msg);
                }
            }

        }
    }

}
