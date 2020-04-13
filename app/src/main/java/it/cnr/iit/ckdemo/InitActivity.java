package it.cnr.iit.ckdemo;

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import it.cnr.iit.ck.CK;
import it.cnr.iit.ck.CKInitiator;

public class InitActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 0;
    private static final String[] RUNTIME_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.PROCESS_OUTGOING_CALLS,
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.WRITE_CALL_LOG,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    Handler handler;
    CKInitiator.OnEventHandler onEventHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);

        handler = new Handler(Looper.getMainLooper());
        onEventHandler = new CKInitiator.OnEventHandler() {

            @Override
            public void onSuccess() {
                Intent intent = new Intent(InitActivity.this.getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                InitActivity.this.finish();
            }

            @Override
            public void onFailure(final int reason) {
                String message;
                switch (reason){
                    case CKInitiator.NO_INTERNET:
                        message = "No cache is generated (or updated) because there isn't internet connection.";
                        break;
                    case CKInitiator.NOT_ALL_CATEGORIES_LOADED:
                        message = "Some package categories hasn't been loaded from network motivation can be:\n" +
                                "- CKInitiatior Executor data too optimistic (# threads and timeout).\n" +
                                "- Endpoint not reachable.\n" +
                                "- Loss of connection during cache creation\n";
                        break;
                    default:
                        message = "An unknown error has occured during cache generation.";
                        break;
                }
                Context applicationContext = InitActivity.this.getApplicationContext();
                Toast toast = Toast.makeText(applicationContext, message, Toast.LENGTH_LONG);
                toast.show();

                Intent intent = new Intent(applicationContext, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                InitActivity.this.finish();
            }
        };

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (permissionsGranted()){
            if (batteryOptimizationMustBeChecked()){
                checkBatteryOptimization();
            } else {
                if (checkAppsUsage()){
                    enableAppsUsage();
                } else {
                    new Thread(new CKInitiator(getApplicationContext(), handler, 1, onEventHandler)).start();
                }
            }
        } else {
            ActivityCompat.requestPermissions(this, RUNTIME_PERMISSIONS, PERMISSION_REQUEST_CODE);
        }
    }

    private boolean permissionsGranted(){
        for(String permission : RUNTIME_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

    private boolean allPermissionsGranted(int[] granted){

        for(int perm : granted)
            if(perm == PackageManager.PERMISSION_DENIED) return false;

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {

        switch (requestCode) {
            case 0: {
                if (allPermissionsGranted(grantResults)) {
                    Log.d(InitActivity.class.getName(), "Permissions granted");
                } else {
                    Log.d(InitActivity.class.getName(), "Doh! Missing some permission.");
                }
            }
        }
    }

    private boolean batteryOptimizationMustBeChecked(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) getApplicationContext().getSystemService(POWER_SERVICE);
            boolean isFreeBatteryUsageDebugVariable = CK.IS_FREE_BATTERY_USAGE_DEBUG_VARIABLE;
            boolean ignoringBatteryOptimizations = pm.isIgnoringBatteryOptimizations(getPackageName());
            return isFreeBatteryUsageDebugVariable && !ignoringBatteryOptimizations;
        }
        return false;
    }

    private void checkBatteryOptimization() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    private boolean checkAppsUsage(){
        boolean isEnabled;
        try {
            Context context = getApplicationContext();
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
            isEnabled = (mode == AppOpsManager.MODE_ALLOWED);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            isEnabled = false;
        }
        return !isEnabled;
    }

    private void enableAppsUsage() {
        startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
    }

}
