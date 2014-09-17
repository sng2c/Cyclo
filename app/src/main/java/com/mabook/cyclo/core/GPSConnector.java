package com.mabook.cyclo.core;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * Created by sng2c on 2014. 9. 17..
 */
public class GPSConnector {
    public static final int MSG_STATUS = 0;
    public static final int MSG_START = 1;
    public static final int MSG_STOP = 2;
    public static final int MSG_PAUSE = 3;
    public static final int MSG_RESUME = 4;

    public static final String ACTION_BIND = "com.mabook.cyclo.core.GPSService.ACTION_BIND";

    public static final int STATE_STOPPED = 1;
    public static final int STATE_STARTED = 2;
    public static final int STATE_PAUSED = 3;
    public static final int STATE_NOT_ALLOWED = 4;

    private final StatusListener mStatusListener;
    BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mStatusListener != null) {
                mStatusListener.onReceiveUpdate(intent.getExtras());
            }
        }
    };
    private final Context mContext;
    private final IncomingHandler handler;
    private final Messenger mClientMessenger;
    private String mBroadcastAction;
    private boolean mBound;
    private Messenger mService;
    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mService = new Messenger(iBinder);
            mBound = true;
            checkStatus();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
            mBound = false;
        }
    };

    public GPSConnector(Context context, StatusListener statusListener) {
        this(context, statusListener, null);
    }

    public GPSConnector(Context context, StatusListener statusListener, String broadcastAction) {
        mContext = context;
        mStatusListener = statusListener;
        mBroadcastAction = broadcastAction;
        if (mBroadcastAction == null) {
            mBroadcastAction = context.getPackageName() + ".ACTION_UPDATE";
        }
        handler = new IncomingHandler();
        mClientMessenger = new Messenger(handler);
    }

    public boolean bindService() {
        Intent service = new Intent(ACTION_BIND);
        mContext.getApplicationContext().registerReceiver(mUpdateReceiver, new IntentFilter(mBroadcastAction));
        return mContext.getApplicationContext().bindService(service, mConnection, BIND_AUTO_CREATE);
    }


    public void unbindService() {
        if (mBound) {
            mContext.getApplicationContext().unbindService(mConnection);
            mContext.getApplicationContext().unregisterReceiver(mUpdateReceiver);
            mBound = false;
        }
    }

    void checkStatus() {
        if (!mBound) return;

        Message msg = Message.obtain(null, MSG_STATUS);
        msg.replyTo = mClientMessenger;
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        start(null, null);
    }

    public void start(Class klass) {
        start(klass, null);
    }

    public void start(String action) {
        start(null, action);
    }

    private void start(Class klass, String action) {
        if (!mBound) return;

        Intent service = new Intent(ACTION_BIND);
        mContext.getApplicationContext().startService(service);

        Message msg = Message.obtain(null, MSG_START);
        msg.replyTo = mClientMessenger;
        Bundle b = new Bundle();
        b.putString("packageName", mContext.getPackageName());
        if (action != null) b.putString("action", action);
        if (klass != null) b.putString("className", klass.getCanonicalName());
        b.putString("startedBy", mContext.getString(mContext.getApplicationInfo().labelRes));
        b.putString("broadcastAction", mBroadcastAction);

        msg.setData(b);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (!mBound) return;

        Intent service = new Intent(ACTION_BIND);
        mContext.getApplicationContext().stopService(service);

        Message msg = Message.obtain(null, MSG_STOP);
        msg.replyTo = mClientMessenger;
        Bundle b = new Bundle();
        b.putString("packageName", mContext.getPackageName());
        msg.setData(b);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        if (!mBound) return;

        Message msg = Message.obtain(null, MSG_PAUSE);
        msg.replyTo = mClientMessenger;
        Bundle b = new Bundle();
        b.putString("packageName", mContext.getPackageName());
        msg.setData(b);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        if (!mBound) return;

        Message msg = Message.obtain(null, MSG_RESUME);
        msg.replyTo = mClientMessenger;
        Bundle b = new Bundle();
        b.putString("packageName", mContext.getPackageName());
        msg.setData(b);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public interface StatusListener {
        void onReceiveStatus(Bundle bundle);

        void onReceiveUpdate(Bundle bundle);
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_STATUS:
                    if (mStatusListener != null) {
                        mStatusListener.onReceiveStatus(msg.getData());
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
