package it.cnr.iit.ck;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

public class CKScheduler {

    private CKScheduler() {}

    /**
     * timeByNow is better than it is at least 9000 because of Android Rules
     * @param timeByNow
     * @param context
     * @param intent
     */
    static void scheduleNextTask(long timeByNow, Context context, Intent intent) {

        PendingIntent pendingIntent = getPendingIntent(context, intent);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // https://developer.android.com/training/monitoring-device-state/doze-standby
            // avoid deferred work in maintenance window of doze mode (from API Level 23 Android 6.0)
            // Neither setAndAllowWhileIdle() nor setExactAndAllowWhileIdle() can fire alarms more than once per 9 minutes, per app.
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            SystemClock.elapsedRealtime() + timeByNow,
                            pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + timeByNow,
                    pendingIntent);
        }
    }

    static void cancelAllSchedules(Context context, Intent intent){
        PendingIntent pendingIntent = getPendingIntent(context, intent);
        ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).cancel(pendingIntent);
        pendingIntent.cancel();
    }

    public static boolean ckIsScheduled(Context context, Intent intent){
        PendingIntent pendingIntent = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                PendingIntent.getForegroundService(context, 762, intent, PendingIntent.FLAG_NO_CREATE) :
                PendingIntent.getService(context, 762, intent, PendingIntent.FLAG_NO_CREATE);
        boolean alarmUp = pendingIntent != null;
        return alarmUp;
    }

    private static PendingIntent getPendingIntent(Context context, Intent intent) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                    PendingIntent.getForegroundService(context, 762, intent, PendingIntent.FLAG_UPDATE_CURRENT) :
                    PendingIntent.getService(context, 762, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
