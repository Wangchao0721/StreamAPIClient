
package com.wangchao.livestream;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class PushDemoActivity extends Activity {

    private String TAG = "com.wangchao.PushDemoActivity";

    private boolean isServiceRunning = false;

    private TextView mTV;
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.live_main);
        mTV = (TextView) findViewById(R.id.text);
        mButton = (Button) findViewById(R.id.button);

        IntentFilter filter = new IntentFilter();
        filter.addAction(PushService.ACTION_PUSHMESSAGE_RECEIVED);
        filter.addAction(PushService.ACTION_PUSHSERVICE_START);
        filter.addAction(PushService.ACTION_PUSHSERVICE_STOP);
        this.registerReceiver(mStatusListener, filter);

        Intent in = new Intent(this, PushService.class);
        in.putExtra("command", PushService.CMDSTART);
        startService(in);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        this.unregisterReceiver(mStatusListener);
    }

    private BroadcastReceiver mStatusListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (action.equals(PushService.ACTION_PUSHMESSAGE_RECEIVED)) {
                mTV.append(intent.getStringExtra("msg"));
            }

            if (action.equals(PushService.ACTION_PUSHSERVICE_START)) {
                mButton.setText("Stop");
            }

            if (action.equals(PushService.ACTION_PUSHSERVICE_STOP)) {
                mButton.setText("Start");
            }
        }
    };

    public void OnStartClick(View v) {

        if (mButton.getText().equals("Stop")) {
            Intent in = new Intent(this, PushService.class);
            in.putExtra("command", PushService.CMDSTOP);
            startService(in);
        } else {
            Intent in = new Intent(this, PushService.class);
            in.putExtra("command", PushService.CMDSTART);
            startService(in);
        }

    }
}
