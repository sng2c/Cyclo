package com.mabook.cyclo.core;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.mabook.cyclo.R;

import static com.mabook.cyclo.core.GPSConnector.MSG_START;
import static com.mabook.cyclo.core.GPSConnector.MSG_STOP;
import static com.mabook.cyclo.core.GPSConnector.MSG_STATUS;
import static com.mabook.cyclo.core.GPSConnector.ACTION_BIND;
import static com.mabook.cyclo.core.GPSConnector.BROADCAST_UPDATE;

public class GPSService extends Service {


    private static final String TAG = "GPSService";

    public GPSService() {
    }

    public boolean started;

    class IncomingHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_START:
                    startLog(msg.getData().getString("startedBy"));
                    sendReply(msg);
                    break;
                case MSG_STOP:
                    stopLog();
                    sendReply(msg);
                    break;
                case MSG_STATUS:
                    sendReply(msg);
                default:
                    super.handleMessage(msg);
            }
        }
    }

    final Messenger mMessenger = new Messenger(new IncomingHandler());

    void sendReply(Message msg){
        if( msg.replyTo != null ){
            Message reply = new Message();
            reply.what = MSG_STATUS;
            Bundle b = new Bundle();
            b.putBoolean("started", started);
            reply.setData(b);
            try {
                msg.replyTo.send(reply);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    void startLog(String startedBy){
        Log.d(TAG, "startLog");
        started = true;

        Notification notification = new Notification.Builder(getApplicationContext())
                .setContentTitle("Cyclo 위치 기록 서비스")
                .setContentText(startedBy + "에 의해 기록 시작됨")
                .setTicker("Cyclo 위치 기록 시작됨")
                .setSmallIcon(R.drawable.ic_launcher)
                .setWhen(System.currentTimeMillis()).build();

        startForeground(1, notification);

        Intent broad = new Intent(BROADCAST_UPDATE);
        broad.putExtra("type","START");
        sendBroadcast(broad);
    }

    void stopLog(){
        Log.d(TAG, "stopLog");
        started = false;
        stopForeground(true);

        Intent broad = new Intent(BROADCAST_UPDATE);
        broad.putExtra("type","STOP");
        sendBroadcast(broad);
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

}
