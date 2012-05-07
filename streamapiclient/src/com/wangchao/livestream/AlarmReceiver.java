
package com.wangchao.livestream;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(PushService.ACTION_ALARM_RECEIVE)) {
            Intent in = new Intent(context, PushService.class);
            in.putExtra("command", PushService.CMDSTART);
            context.startService(in);
        }

    }

}
