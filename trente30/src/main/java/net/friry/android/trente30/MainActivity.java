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

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";
    private final static boolean LOCAL_LOGV = false;

    public final static String WARMUP_DURATION = "net.friry.android.Trente30.WARMUP_DURATION";
    public final static long WARMUP_DURATION_default = 600;
    public final static String ITERATION_COUNT = "net.friry.android.Trente30.ITERATION_COUNT";
    public final static long ITERATION_COUNT_default = 15;
    public final static String FAST_DURATION = "net.friry.android.Trente30.FAST_DURATION";
    public final static long FAST_DURATION_default = 30;
    public final static String SLOW_DURATION = "net.friry.android.Trente30.SLOW_DURATION";
    public final static long SLOW_DURATION_default = 30;
    public final static String KEEP_SCREEN_ON = "net.friry.android.Trente30.KEEP_SCREEN_ON";
    public final static boolean KEEP_SCREEN_ON_default = false;
    public final static String LOUDER = "net.friry.android.Trente30.LOUDER";
    public final static boolean LOUDER_default = false;

    private EditText warmupDurationText;
    private long warmupDuration;
    private EditText iterationCountText;
    private long iterationCount;
    private EditText fastDurationText;
    private long fastDuration;
    private EditText slowDurationText;
    private long slowDuration;

    private MenuItem keepScreenOnItem;
    private boolean keepScreenOn;

    private MenuItem louderItem;
    private boolean louder;

    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (LOCAL_LOGV) { Log.v(TAG, "Beginning onCreate"); }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        warmupDurationText = (EditText) findViewById(R.id.warmup_duration_text);
        iterationCountText = (EditText) findViewById(R.id.iteration_count_text);
        fastDurationText = (EditText) findViewById(R.id.fast_duration_text);
        slowDurationText = (EditText) findViewById(R.id.slow_duration_text);

        Log.d(TAG, "Getting preferences");
        sharedPref = getPreferences(Context.MODE_PRIVATE);
        getValuesFromPrefs();
        setValuesToFields();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (LOCAL_LOGV) { Log.v(TAG, "Beginning onCreateOptionsMenu"); }

        getMenuInflater().inflate(R.menu.menu_main, menu);

        keepScreenOnItem = menu.findItem(R.id.action_keepscreenon);
        keepScreenOnItem.setChecked(keepScreenOn);

        louderItem = menu.findItem(R.id.action_louder);
        louderItem.setChecked(louder);

        return true;
    }

    public void loadDefaults(MenuItem menuItem) {
        if (LOCAL_LOGV) { Log.v(TAG, "Beginning loadDefaults"); }

        getValuesFromPrefs();
        setValuesToFields();
    }

    public void saveDefaults(MenuItem menuItem) {
        if (LOCAL_LOGV) { Log.v(TAG, "Beginning saveDefaults"); }


        getValuesFromFields();
        setValuesToPrefs();
    }

    public void toggleScreenOnOff(MenuItem menuItem) {
        if (LOCAL_LOGV) { Log.v(TAG, "Beginning toggleScreenOnOff"); }

        if (keepScreenOn) {
            Log.d(TAG, "Receive setting: screen must NOT be kept on");
            keepScreenOn = false;
            menuItem.setChecked(false);
        } else {
            keepScreenOn = true;
            Log.d(TAG, "Receiving setting: screen must be kept on");
            menuItem.setChecked(true);
        }
    }

    public void toggleLouder(MenuItem menuItem) {
        if (LOCAL_LOGV) { Log.v(TAG, "Beginning toggleLouder"); }

        if (louder) {
            Log.d(TAG, "Receive setting: louder off");
            louder = false;
            menuItem.setChecked(false);
        } else {
            Log.d(TAG, "Receiving setting: louder on");
            louder = true;
            menuItem.setChecked(true);
        }
    }

    public void goMessage(View view) {
        if (LOCAL_LOGV) { Log.v(TAG, "Beginning goMessage"); }

        Intent intent = new Intent(this, RunActivity.class);

        getValuesFromFields();
        intent.putExtra(WARMUP_DURATION, warmupDuration);
        intent.putExtra(ITERATION_COUNT, iterationCount);
        intent.putExtra(FAST_DURATION, fastDuration);
        intent.putExtra(SLOW_DURATION, slowDuration);
        intent.putExtra(KEEP_SCREEN_ON, keepScreenOn);
        intent.putExtra(LOUDER, louder);

        Log.d(TAG, "Starting run activity (warmupDuration="+Long.toString(warmupDuration)+", iterationCount="+Long.toString(iterationCount)+", fastDuration="+Long.toString(fastDuration)+", slowDuration="+Long.toString(slowDuration)+", keepScreenOn="+Boolean.toString(keepScreenOn)+")");
        startActivity(intent);
    }

    private void getValuesFromFields() {
        if (LOCAL_LOGV) { Log.v(TAG, "Beginning getValuesFromFields"); }

        // warmup_duration field
        try {
            warmupDuration = Long.parseLong(warmupDurationText.getText().toString());
        } catch (NumberFormatException e) {
            warmupDuration = WARMUP_DURATION_default;
        }

        // iteration_count field
        try {
            iterationCount = Long.parseLong(iterationCountText.getText().toString());
        } catch (NumberFormatException e) {
            iterationCount = ITERATION_COUNT_default;
        }

        // fast_duration field
        try {
            fastDuration = Long.parseLong(fastDurationText.getText().toString());
        } catch (NumberFormatException e) {
            fastDuration = FAST_DURATION_default;
        }

        // slow_duration field
        try {
            slowDuration = Long.parseLong(slowDurationText.getText().toString());
        } catch (NumberFormatException e) {
            slowDuration = SLOW_DURATION_default;
        }
        Log.d(TAG, "Retrieved prefs from GUI (warmupDuration=" + Long.toString(warmupDuration) + ", iterationCount=" + Long.toString(iterationCount) + ", fastDuration=" + Long.toString(fastDuration) + ", slowDuration=" + Long.toString(slowDuration) + ", keepScreenOn=" + Boolean.toString(keepScreenOn) + ")");
    }

    private void setValuesToFields() {
        if (LOCAL_LOGV) { Log.v(TAG, "Beginning setValuesToFields"); }

        Log.d(TAG, "Applying prefs to GUI (warmupDuration="+Long.toString(warmupDuration)+", iterationCount="+Long.toString(iterationCount)+", fastDuration="+Long.toString(fastDuration)+", slowDuration="+Long.toString(slowDuration)+", keepScreenOn="+Boolean.toString(keepScreenOn)+")");
        warmupDurationText.setText(Long.toString(warmupDuration));
        iterationCountText.setText(Long.toString(iterationCount));
        fastDurationText.setText(Long.toString(fastDuration));
        slowDurationText.setText(Long.toString(slowDuration));
        if (keepScreenOnItem != null) {
            // At first call, from onCreate, menu is not created yet
            keepScreenOnItem.setChecked(keepScreenOn);
        }
        if (louderItem != null) {
            // At first call, from onCreate, menu is not created yet
            louderItem.setChecked(louder);
        }
    }

    private void getValuesFromPrefs() {
        if (LOCAL_LOGV) { Log.v(TAG, "Beginning getValuesFromPrefs"); }

        warmupDuration = sharedPref.getLong(WARMUP_DURATION, WARMUP_DURATION_default);
        iterationCount = sharedPref.getLong(ITERATION_COUNT, ITERATION_COUNT_default);
        fastDuration = sharedPref.getLong(FAST_DURATION, FAST_DURATION_default);
        slowDuration = sharedPref.getLong(SLOW_DURATION, SLOW_DURATION_default);
        keepScreenOn = sharedPref.getBoolean(KEEP_SCREEN_ON, KEEP_SCREEN_ON_default);
        louder = sharedPref.getBoolean(LOUDER, LOUDER_default);
        Log.d(TAG, "Loaded prefs (warmupDuration=" + Long.toString(warmupDuration) + ", iterationCount=" + Long.toString(iterationCount) + ", fastDuration=" + Long.toString(fastDuration) + ", slowDuration=" + Long.toString(slowDuration) + ", keepScreenOn=" + Boolean.toString(keepScreenOn) + ", louder=" + Boolean.toString(louder) + ")");
    }

    private void setValuesToPrefs() {
        if (LOCAL_LOGV) { Log.v(TAG, "Beginning setValuesToPrefs"); }
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(WARMUP_DURATION, warmupDuration);
        editor.putLong(ITERATION_COUNT, iterationCount);
        editor.putLong(FAST_DURATION, fastDuration);
        editor.putLong(SLOW_DURATION, slowDuration);
        editor.putBoolean(KEEP_SCREEN_ON, keepScreenOn);
        editor.putBoolean(LOUDER, louder);
        Log.d(TAG, "Saving prefs (warmupDuration=" + Long.toString(warmupDuration) + ", iterationCount=" + Long.toString(iterationCount) + ", fastDuration=" + Long.toString(fastDuration) + ", slowDuration=" + Long.toString(slowDuration) + ", keepScreenOn=" + Boolean.toString(keepScreenOn) + ", louder=" + Boolean.toString(louder) + ")");
        editor.apply();
    }
}
