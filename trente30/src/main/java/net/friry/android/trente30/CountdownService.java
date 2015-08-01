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

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

public class CountdownService extends Service {
    private final static String TAG = "CountdownService";
    private final static boolean LOCAL_LOGV = false;

    private final static float VOLUME = (float) 0.7;

    public final static String COUNTDOWN_BROADCAST = "net.friry.android.trente30.countdown_broadcast";
    public final static String COUNTDOWN_STEP = "net.friry.android.trente30.countdown_step";
    public final static String COUNTDOWN_REMAINING_TIME = "net.friry.android.trente30.countdown_remaining_time";

    private long warmupDuration;
    private long iterationCount;
    private long fastDuration;
    private long slowDuration;

    private SoundPool soundPool;
    private int fastSound;
    private int slowSound;
    private int endSound;

    private CountDownTimer countdownTimer = null;

    private int step;

    @Override
    public void onCreate() {
        if (LOCAL_LOGV) { Log.v(TAG, "Beginning onCreate"); }

        super.onCreate();

        /* for API>21
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
                .build();
        soundPool = new SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                .build();
        */
        Log.d(TAG, "Creating soundPool created");
        soundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
        fastSound = soundPool.load(this, R.raw.fast, 1);
        slowSound = soundPool.load(this, R.raw.slow, 1);
        endSound = soundPool.load(this, R.raw.end, 1);
    }

    @Override
    public void onDestroy() {
        if (LOCAL_LOGV) { Log.v(TAG, "Beginning onDestroy"); }

        if (countdownTimer != null) {
            Log.d(TAG, "Cancelling countdownTimer");
            countdownTimer.cancel();
            countdownTimer = null;
        }

        if (soundPool != null) {
            Log.d(TAG, "Releasing soundPool");
            soundPool.release();
            soundPool = null;
        }

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (LOCAL_LOGV) { Log.v(TAG, "Beginning onStartCommand"); }

        super.onStartCommand(intent, flags, startId);

        warmupDuration = intent.getLongExtra(MainActivity.WARMUP_DURATION, MainActivity.WARMUP_DURATION_default);
        iterationCount = intent.getLongExtra(MainActivity.ITERATION_COUNT, MainActivity.ITERATION_COUNT_default);
        fastDuration = intent.getLongExtra(MainActivity.FAST_DURATION, MainActivity.FAST_DURATION_default);
        slowDuration = intent.getLongExtra(MainActivity.SLOW_DURATION, MainActivity.SLOW_DURATION_default);

        Log.d(TAG, "Received intent (warmupDuration="+Long.toString(warmupDuration)+", iterationCount="+Long.toString(iterationCount)+", fastDuration="+Long.toString(fastDuration)+", slowDuration="+Long.toString(slowDuration)+")");

        Log.d(TAG,"Starting steps");
        nextStep();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (LOCAL_LOGV) { Log.v(TAG, "Beginning onBind"); }

        return null;
    }

    private void runCountdown(long duration) {
        if (LOCAL_LOGV) { Log.v(TAG, "Beginning runCountdown"); }

        countdownTimer = new CountDownTimer(duration, 250) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d(TAG, "Countdown seconds remaining: " + millisUntilFinished / 1000);
                Intent broadcastIntent = new Intent(COUNTDOWN_BROADCAST);
                broadcastIntent.putExtra(COUNTDOWN_STEP, step);
                broadcastIntent.putExtra(COUNTDOWN_REMAINING_TIME, millisUntilFinished);
                Log.d(TAG, "Broadcasting tick (step=" + step + ", millisUntilFinished=" + millisUntilFinished+")");
                sendBroadcast(broadcastIntent);
            }

            @Override
            public void onFinish() {
                Log.d(TAG, "Finished countdown (step="+step+")");
                Log.d(TAG, "Starting next step");
                nextStep();
            }
        };
        countdownTimer.start();
    }

    private void nextStep() {
        if (LOCAL_LOGV) { Log.v(TAG, "Beginning nextStep"); }

        step += 1;
        Log.d(TAG, "Incrementing step (step="+step+")");
        if (step == 1) {
            Log.i(TAG, "Starting countdowns");
            Log.d(TAG, "Starting countdown (duration="+warmupDuration+")");
            runCountdown(warmupDuration*1000);
        } else if (step > 1+2*iterationCount) {
            Log.d(TAG, "Playing end sound");
            soundPool.play(endSound, VOLUME, VOLUME, 0, 0, 1);
            Intent broadcastIntent = new Intent(COUNTDOWN_BROADCAST);
            broadcastIntent.putExtra(COUNTDOWN_STEP, 0);
            broadcastIntent.putExtra(COUNTDOWN_REMAINING_TIME, (long) 0);
            sendBroadcast(broadcastIntent);
            Log.d(TAG, "Broadcasting end");
            Log.i(TAG, "Finished countdowns");
            // wait that sound is played
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Log.d(TAG, "Failed to sleep");
            }
            stopSelf();
        } else if ((step - 1) % 2 == 1) {
            Log.d(TAG, "Starting countdown (duration="+fastDuration+")");
            runCountdown(fastDuration * 1000);
            Log.d(TAG, "Playing fast sound");
            soundPool.play(fastSound, VOLUME, VOLUME, 0, 0, 1 );
        } else if ((step - 1) % 2 == 0) {
            Log.d(TAG, "Starting countdown (duration="+slowDuration+")");
            runCountdown(slowDuration * 1000);
            Log.d(TAG, "Playing slow sound");
            soundPool.play(slowSound, VOLUME, VOLUME, 0, 0, 1 );
        }
    }
}
