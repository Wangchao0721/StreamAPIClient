
package com.wangchao.livestream;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent in = new Intent(context, AlarmReceiver.class);
            in.setAction(PushService.ACTION_ALARM_RECEIVE);
            PendingIntent sender = PendingIntent.getBroadcast(context, 0, in, 0);
            long firstime = SystemClock.elapsedRealtime();
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstime, 10 * 1000, sender);
        }

    }

}
