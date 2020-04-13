/*
 *   Copyright (c) 2017. Mattia Campana, mattia.campana@iit.cnr.it, Franca Delmastro, franca.delmastro@gmail.com
 *
 *   This file is part of ContextKit.
 *
 *   ContextKit (CK) is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   ContextKit (CK) is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with ContextKit (CK).  If not, see <http://www.gnu.org/licenses/>.
 */

package it.cnr.iit.ck;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import it.cnr.iit.ck.commons.Utils;
import it.cnr.iit.ck.features.PlayStoreNetworking;
import it.cnr.iit.ck.features.PlayStoreStorage;

@SuppressWarnings("unused")
public class CK {

    // https://developer.android.com/training/monitoring-device-state/doze-standby
    public static final boolean IS_FREE_BATTERY_USAGE_DEBUG_VARIABLE = true;

    // to log multiloggable data also if it is empty
    public static final boolean LOG_EMPTY_MULTI_LOGGABLE_DATA = true;

    // Intent's keys of configurations, type of boot and CK periodic
    static final String CONFIGURATION_INTENT_KEY = "CONFIGURATION_INTENT_KEY";
    static final String TRIGGER_EVERY_TIME_INTENT_KEY = "TRIGGER_EVERY_TIME_INTENT_KEY";
    static final String DURATION_INTENT_KEY = "DURATION_INTENT_KEY";

    public static boolean running = false;

    private CK() {};

    /**
     * Start CK service
     * @param context: application context
     * @param configuration: configuration in json format
     */
    public static void start(Context context, String configuration, boolean runOnReboot){
        if(!running) {
            running = true;
            Intent intent = new Intent(context, OneTimeRunCKManager.class);
            intent.putExtra(CONFIGURATION_INTENT_KEY, configuration);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
        }
    }

    /**
     * Stop CK service
     * @param context: application context
     */
    public static void stop(Context context) {
        if(running) {
            running = false;
            context.stopService(new Intent(context, OneTimeRunCKManager.class));
        }
    }

    /**
     * start CK service for duration time every triggerEveryTime
     * @param context: application context
     * @param configuration: configuration in json format
     * @param duration: milliseconds
     * @param triggerEveryTime milliseconds
     */
    public static void startPeriodically(Context context, String configuration, long duration,
                                         long triggerEveryTime, boolean runOnReboot){
        if(!running) {
            running = true;
            Intent intent = buildIntent(context, configuration, duration, triggerEveryTime);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
        }
    }

    /**
     * Stop a periodic CK service
     * @param context: application context
     */
    public static void stopPeriodically(Context context){
        if(running){
            running = false;
            Intent cancelServiceIntent = new Intent(context, PeriodicRunCKManager.class);
            CKScheduler.cancelAllSchedules(context, cancelServiceIntent);
            context.stopService(cancelServiceIntent);
        }
    }

    public static boolean isRunning(Context applicationContext){
        boolean isScheduled = CKScheduler.ckIsScheduled(applicationContext, new Intent(applicationContext, PeriodicRunCKManager.class));
        return running || isScheduled;
    }

    public static Intent buildIntent(Context applicationContext, String configuration, long duration, long triggerEveryTime) {
        Intent intent = new Intent(applicationContext, PeriodicRunCKManager.class);
        intent.putExtra(CONFIGURATION_INTENT_KEY, configuration);
        intent.putExtra(DURATION_INTENT_KEY, duration);
        intent.putExtra(TRIGGER_EVERY_TIME_INTENT_KEY, triggerEveryTime);
        return intent;
    }

}
