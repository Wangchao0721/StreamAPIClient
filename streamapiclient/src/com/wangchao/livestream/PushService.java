
package com.wangchao.livestream;

import com.wangchao.livestream.util.Tools;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * Twitter Streaming API Like Service
 * 
 * @author wangchao
 */
public class PushService extends Service {

    /************************************************************************/
    /* CONSTANTS */
    /************************************************************************/
    private String TAG = "com.wangchao.livestream";

    public static final String HOST_URL_PUSHMESSAGE = "http://5.5.5.121:8081/";

    /************************************************************************/
    /* Action when state changes */
    /************************************************************************/
    public static final String ACTION_PUSHMESSAGE_RECEIVED = "com.wangchao.action.pushmessage";
    public static final String ACTION_PUSHSERVICE_START = "com.wangchao.action.pushservice.start";
    public static final String ACTION_PUSHSERVICE_STOP = "com.wangchao.action.pushservice.stop";

    public static final String ACTION_ALARM_RECEIVE = "com.wangchao.action.alerm";
    /************************************************************************/
    /* Message represent service states changed */
    /************************************************************************/
    public static final int PUSHMESSAGE_RECEIVED = 0;
    public static final int PUSHSERVICE_START = 1;
    public static final int PUSHSERVICE_STOP = 2;

    /************************************************************************/
    /* Commands to control service states */
    /************************************************************************/
    public static final String SERVICECMD = "com.wangchao.pushservice.servicecmd";
    public static final String CMDSTART = "start";
    public static final String CMDSTOP = "stop";

    /************************************************************************/
    /* Values to maintain states */
    /************************************************************************/
    private boolean mKeepRunning = false;

    public boolean isPushServiceRunning() {

        return mKeepRunning;
    }

    /************************************************************************/
    /* Local values to dealwith actions */
    /************************************************************************/
    private PushHandler mHandler = new PushHandler();
    private ReceiveThread mReceiveThread = null;

    /************************************************************************/
    /* Values to store results */
    /************************************************************************/
    private ArrayList<String> mResults;

    /************************************************************************/
    /* METHODS - core Service lifecycle methods */
    /************************************************************************/

    @Override
    public void onCreate() {

        super.onCreate();

        Tools.debugLog(TAG, "Service on create");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            String action = intent.getAction();
            String cmd = intent.getStringExtra("command");

            Tools.debugLog(TAG, "Service onStartCommand: " + cmd);

            if (CMDSTART.equals(cmd)) {
                if (!mKeepRunning) {
                    mKeepRunning = true;
                    startThread();
                    notifyChange(ACTION_PUSHSERVICE_START, null);
                }
            } else if (CMDSTOP.equals(cmd)) {
                if (mKeepRunning) {
                    mKeepRunning = false;
                    stopThread();
                    notifyChange(ACTION_PUSHSERVICE_STOP, null);
                }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    /************************************************************************/
    /* Method to handle&notify all push service actions */
    /************************************************************************/

    /**
     * Handler to process all push service actions
     * 
     * @author wangchao
     */
    private class PushHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {

            Tools.debugLog(TAG, "PushHandler:" + "receive " + msg.what);

            switch (msg.what) {

                case PUSHMESSAGE_RECEIVED:
                    // Send Broadcast to other activities
                    notifyChange(ACTION_PUSHMESSAGE_RECEIVED, msg.getData().getString("msg"));

                    // Status Bar Notification
                    String ns = Context.NOTIFICATION_SERVICE;
                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);

                    int icon = R.drawable.ic_launcher;
                    CharSequence tickerText = "Live Streaming";
                    long when = System.currentTimeMillis();

                    Notification notification = new Notification(icon, tickerText, when);

                    Context context = getApplicationContext();
                    CharSequence contentTitle = "Live Streaming";
                    CharSequence contentText = "Receive: " + msg.getData().getString("msg");
                    Intent notificationIntent = new Intent(PushService.this, PushDemoActivity.class);
                    PendingIntent contentIntent = PendingIntent.getActivity(PushService.this, 0,
                            notificationIntent, 0);

                    notification.setLatestEventInfo(context, contentTitle, contentText,
                            contentIntent);

                    final int HELLO_ID = 1;

                    mNotificationManager.notify(HELLO_ID, notification);
                    break;
                default:
                    break;

            }
        }
    }

    /**
     * Notify the change-receivers that something has changed.
     * 
     * @param what
     * @author wangchao
     */
    private void notifyChange(String what, String msg) {

        Intent intent = new Intent(what);
        intent.putExtra("msg", msg);
        sendBroadcast(intent);
    }

    /************************************************************************/
    /* Thread related method to start & run & stop */
    /************************************************************************/

    /**
     * Thread run background as pesistente tcp/ip to receive push message
     * 
     * @author wangchao
     */
    private class ReceiveThread extends Thread {

        @Override
        public void run() {

            Tools.debugLog(TAG, "ReceiveThread run");
            try {
                URL url = new URL(HOST_URL_PUSHMESSAGE);
                URLConnection urlConnection = url.openConnection();
                InputStream in = (InputStream) urlConnection.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                parseMessage(reader);
                Tools.debugLog(TAG, "Service parseMessage done");
                in.close();
            } catch (Exception e) {
                Tools.debugLog(TAG, "ReceiveThread: " + e.toString());
            }
        }

        private void parseMessage(BufferedReader reader) {

            String line = "";

            do {
                try {
                    line = reader.readLine();

                    Tools.debugLog(TAG, "parseMessage: " + line);

                    Message msg = new Message();
                    msg.what = PUSHMESSAGE_RECEIVED;
                    Bundle data = new Bundle();
                    data.putString("msg", line);
                    msg.setData(data);
                    mHandler.sendMessage(msg);
                } catch (IOException e) {
                    Tools.debugLog(TAG, "ParseMessage: " + e.toString());
                }

            } while (mKeepRunning && line.length() > 0);
        }

    }

    public synchronized void startThread() {

        if (mReceiveThread == null) {
            mReceiveThread = new ReceiveThread();
            mReceiveThread.start();
        }
    }

    public synchronized void stopThread() {

        if (mReceiveThread != null) {
            ReceiveThread moribund = mReceiveThread;
            mReceiveThread = null;
            moribund.interrupt();
        }
    }
}
