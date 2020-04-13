package it.cnr.iit.ck;

import android.app.Service;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

public class OneTimeRunCKManager extends CKManager {

    private PowerManager.WakeLock wakeLock;

    @Override
    public void onCreate() {
        super.onCreate();
        promoteToForegroundService();
        if (CK.IS_FREE_BATTERY_USAGE_DEBUG_VARIABLE) {
            wakeLock = ((PowerManager) getApplicationContext().getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "CK::OneTimeRunCKManager");
            wakeLock.acquire();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        CK.running = true;
        super.onStartCommand(intent, flags, startId);

        String configuration = getConfiguration(intent);
        parseConfiguration(configuration);

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        if (CK.IS_FREE_BATTERY_USAGE_DEBUG_VARIABLE) { wakeLock.release();}
    }
}
