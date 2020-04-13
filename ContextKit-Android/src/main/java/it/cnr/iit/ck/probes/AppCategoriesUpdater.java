package it.cnr.iit.ck.probes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import it.cnr.iit.ck.features.PlayStoreNetworking;
import it.cnr.iit.ck.features.PlayStoreStorage;

/**
 * This object is instantiated during CK startup, try one time to update app cache (if internet is
 * disponible), then it checks for app install or uninstall
 */
public class AppCategoriesUpdater extends ContinuousProbe {

    private static final long HTTP_REQUEST_TIMEOUT = 20000;

    private  PlayStoreNetworking playStoreNetworking;
    private BroadcastReceiver appReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction() != null &&
                    (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED) ||
                            intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED))){
                updateCache();
            }
        }
    };

    private void updateCache() {
        final PackageManager pm = getContext().getPackageManager();
        for (ApplicationInfo applicationInfo: pm.getInstalledApplications(PackageManager.GET_META_DATA)){
            String packageName = applicationInfo.packageName;
            final String category = PlayStoreStorage.readAppCategory(getContext(), packageName);
            if (category == null) {
                try {
                    System.out.println("dummy");
                    playStoreNetworking.cacheAppCategorySinchronously(packageName, HTTP_REQUEST_TIMEOUT);
                    //playStoreNetworking.cacheAppCategory(packageName);
                } catch (TimeoutException | ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e){
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    @Override
    public void init() {
        playStoreNetworking = new PlayStoreNetworking(getContext());

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addDataScheme("package");

        getContext().registerReceiver(appReceiver, intentFilter, null, getHandler());
    }

    @Override
    public void exec() {
        updateCache();
    }

    @Override
    public void onFirstRun() { }

    @Override
    void onStop() {
        getContext().unregisterReceiver(appReceiver);
    }

    @Override
    public boolean featuresData() {
        return false;
    }

    @Override
    public String[] getLogFileHeaders() {
        return new String[0];
    }
}
