/*
    Copyright (c) 2015 Benoit Friry

    This file is part of Trente30.

    Trente30 is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Trente30 is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.friry.android.trente30;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;


public class RunActivity extends Activity {
    private final static String TAG = "RunActivity";
    private final static boolean LOCAL_LOGV = false;

    private TextView countdownTextView;
    private TextView stepView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (LOCAL_LOGV) { Log.v(TAG, "Beginning onCreate"); }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);

        Intent intent = getIntent();
        long warmupDuration = intent.getLongExtra(MainActivity.WARMUP_DURATION, MainActivity.WARMUP_DURATION_default);
        long iterationCount = intent.getLongExtra(MainActivity.ITERATION_COUNT, MainActivity.ITERATION_COUNT_default);
        long fastDuration = intent.getLongExtra(MainActivity.FAST_DURATION, MainActivity.FAST_DURATION_default);
        long slowDuration = intent.getLongExtra(MainActivity.SLOW_DURATION, MainActivity.SLOW_DURATION_default);
        boolean keepScreenOn = intent.getBooleanExtra(MainActivity.KEEP_SCREEN_ON, MainActivity.KEEP_SCREEN_ON_default);

        Log.d(TAG, "Received intent (warmupDuration="+Long.toString(warmupDuration)+", iterationCount="+Long.toString(iterationCount)+", fastDuration="+Long.toString(fastDuration)+", slowDuration="+Long.toString(slowDuration)+", keepScreenOn="+Boolean.toString(keepScreenOn)+")");

        if (keepScreenOn) {
            Log.d(TAG, "Setting screen to be kept on");
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        countdownTextView = (TextView) findViewById(R.id.countdown_value);
        stepView = (TextView) findViewById(R.id.step_value);

        Intent countdownService = new Intent(this, CountdownService.class);
        countdownService.putExtra(MainActivity.WARMUP_DURATION, warmupDuration);
        countdownService.putExtra(MainActivity.ITERATION_COUNT, iterationCount);
        countdownService.putExtra(MainActivity.FAST_DURATION, fastDuration);
        countdownService.putExtra(MainActivity.SLOW_DURATION, slowDuration);

        Log.d(TAG, "Starting countdown service (warmupDuration=" + Long.toString(warmupDuration) + ", iterationCount=" + Long.toString(iterationCount) + ", fastDuration=" + Long.toString(fastDuration) + ", slowDuration=" + Long.toString(slowDuration) + ")");
        startService(countdownService);
    }

    @Override
    public void onResume() {
        if (LOCAL_LOGV) { Log.v(TAG, "Beginning onResume"); }

        super.onResume();
        Log.d(TAG, "Registering broadcast receiver");
        registerReceiver(br, new IntentFilter(CountdownService.COUNTDOWN_BROADCAST));
    }

    @Override
    public void onPause() {
        if (LOCAL_LOGV) { Log.v(TAG, "Beginning onPause"); }

        super.onPause();
        Log.d(TAG, "Unregistering broadcast receiver");
        unregisterReceiver(br);

        // when resuming, GUI will show end if countdownService is finished and do not broadcast new status
        stepView.setText(R.string.end_label);
        countdownTextView.setText("0");
    }

    @Override
    public void onStop() {
        if (LOCAL_LOGV) { Log.v(TAG, "Beginning onStop"); }

        try {
            Log.d(TAG, "Unregistering broadcast receiver");
            unregisterReceiver(br);
        } catch (Exception e) {
            // Receiver was probably already stopped in onPause()
            Log.d(TAG, "Impossible to unregistering broadcast receiver (probably already done)");
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (LOCAL_LOGV) { Log.v(TAG, "Beginning onDestroy"); }

        Log.d(TAG, "Stopping countdown service");
        stopService(new Intent(this, CountdownService.class));
        super.onDestroy();
    }

    private final BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (LOCAL_LOGV) { Log.v(TAG, "Beginning onReceive"); }
            if (intent.getExtras() != null) {
                int step = intent.getIntExtra(CountdownService.COUNTDOWN_STEP, 0);
                long millisUntilFinished = intent.getLongExtra(CountdownService.COUNTDOWN_REMAINING_TIME, 0);

                Log.d(TAG, "Processing broadcast (step=" + step + ", millisUntilFinished=" + millisUntilFinished + ")");

                if (step == 1) {
                    stepView.setText(R.string.warmup_label);
                } else if (step == 0 ) {
                    stepView.setText(R.string.end_label);
                } else if ((step - 1) % 2 == 1) {
                    stepView.setText(R.string.fast_label);
                } else if ((step - 1) % 2 == 0) {
                    stepView.setText(R.string.slow_label);
                }

                countdownTextView.setText(Long.toString(millisUntilFinished/1000));
            }
        }
    };
}
