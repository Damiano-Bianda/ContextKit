package it.cnr.iit.ck;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

public class PeriodicRunCKManager extends CKManager {

    private Thread serviceDurationThread;
    private PowerManager.WakeLock wakeLock;

    @Override
    public void onCreate() {
        super.onCreate();

        promoteToForegroundService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        CK.running = true;
        super.onStartCommand(intent, flags, startId);

        String configuration = getConfiguration(intent);
        parseConfiguration(configuration);

        Context context = getApplicationContext();
        final long triggerEveryTime = intent.getLongExtra(CK.TRIGGER_EVERY_TIME_INTENT_KEY, -1);
        final long duration = intent.getLongExtra(CK.DURATION_INTENT_KEY, -1);

        Intent nextIntent = CK.buildIntent(context, configuration, duration, triggerEveryTime);
        CKScheduler.scheduleNextTask(triggerEveryTime, context, nextIntent);

        if (CK.IS_FREE_BATTERY_USAGE_DEBUG_VARIABLE) {
            wakeLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "CK::PeriodicRunCKManager");
            long offset = (long) (duration * 0.1);
            wakeLock.acquire(duration + offset);
        }

        serviceDurationThread = new Thread(() -> {
            try {
                Thread.sleep(duration);
                stopSelf();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        serviceDurationThread.start();
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        serviceDurationThread.interrupt();
        if (CK.IS_FREE_BATTERY_USAGE_DEBUG_VARIABLE) { wakeLock.release(); }
    }
}
