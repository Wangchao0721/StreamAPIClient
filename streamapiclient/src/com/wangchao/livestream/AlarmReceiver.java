
package com.wangchao.livestream;

import com.wangchao.livestream.util.Tools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
    /************************************************************************/
    /* CONSTANTS */
    /************************************************************************/
    private String TAG = "com.wangchao.livestream";

    @Override
    public void onReceive(Context context, Intent intent) {

        Tools.debugLog(TAG, "AlarmReceiver: onReceive");

        if (intent.getAction().equals(PushService.ACTION_ALARM_RECEIVE)) {

            Intent in = new Intent(context, PushService.class);
            in.putExtra("command", PushService.CMDSTART);
            context.startService(in);
        }

    }
}
