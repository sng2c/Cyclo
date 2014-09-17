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
    public static final int MSG_START = 1;
    public static final int MSG_STOP = 2;
    public static final int MSG_STATUS = 3;
    public static final String ACTION_BIND = "com.mabook.cyclo.core.GPSService.ACTION_BIND";
    public static final String BROADCAST_UPDATE = "com.mabook.cyclo.core.GPSService.BROADCAST_UPDATE";

    private final StatusListener mStatusListener;
    private final Context mContext;
    private final IncomingHandler handler;
    private final Messenger mClientMessenger;
    private boolean mBound;

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

    public GPSConnector(Context context, StatusListener statusListener) {
        mContext = context;
        mStatusListener = statusListener;
        handler = new IncomingHandler();
        mClientMessenger = new Messenger(handler);
    }

    BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(mStatusListener != null){
                mStatusListener.onReceiveUpdate(intent.getExtras());
            }
        }
    };

    public boolean bindService() {
        Intent service = new Intent(ACTION_BIND);

        mContext.getApplicationContext().registerReceiver(mUpdateReceiver, new IntentFilter(BROADCAST_UPDATE));

        return mContext.getApplicationContext().bindService(service, mConnection, BIND_AUTO_CREATE);
    }


    public void unbindService() {
        if (mBound) {
            mContext.getApplicationContext().unbindService(mConnection);
            mContext.getApplicationContext().unregisterReceiver(mUpdateReceiver);
            mBound = false;
        }
    }

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

    public void start(String startedBy) {
        if (!mBound) return;

        Intent service = new Intent(ACTION_BIND);
        mContext.getApplicationContext().startService(service);

        Message msg = Message.obtain(null, MSG_START);
        msg.replyTo = mClientMessenger;
        Bundle b = new Bundle();
        b.putString("startedBy",startedBy);
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
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


}
