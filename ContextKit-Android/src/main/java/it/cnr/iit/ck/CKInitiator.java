package it.cnr.iit.ck;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import it.cnr.iit.ck.commons.Utils;
import it.cnr.iit.ck.features.PlayStoreNetworking;
import it.cnr.iit.ck.features.PlayStoreStorage;

/**
 * this class requires android.permission.ACCESS_NETWORK_STATE
 */
@SuppressLint("MissingPermission")
public class CKInitiator implements Runnable {

    public static final int NO_INTERNET = 0;
    public static final int NOT_ALL_CATEGORIES_LOADED = 1;

    private static final int HTTP_REQUEST_NUM_THREADS = 6;
    private static final long HTTP_REQUEST_TIMEOUT = 20000;
    private static final long EXECUTOR_SHUTDOWN_TIMEOUT = 60000;

    private final Context applicationContext;
    private final Handler handler;
    private final int retry;
    private final OnEventHandler onEventHandler;;

    public CKInitiator(final Context applicationContext, final Handler handler, int retry, final OnEventHandler onEventHandler){
        this.applicationContext = applicationContext;
        this.handler = handler;
        this.retry = retry;
        this.onEventHandler = onEventHandler;
    }

    public interface OnEventHandler{
        void onSuccess();
        void onFailure(final int reason);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void run() {

        InitCKSharedPreferences.generateFirstTimeData(applicationContext);

        if (!isNetworkAvailable()){
            handler.post(() -> onEventHandler.onFailure(NO_INTERNET));
        }

        final PlayStoreNetworking playStoreNetworking = new PlayStoreNetworking(applicationContext);

        for(int i = 0; i < retry; i++) {
            boolean success = updateCategoriesCache(playStoreNetworking);
            if(success) {
                handler.post(onEventHandler::onSuccess);
                return;
            }
        }

        handler.post(() -> onEventHandler.onFailure(NOT_ALL_CATEGORIES_LOADED));

    }

    private boolean updateCategoriesCache(PlayStoreNetworking playStoreNetworking) {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(HTTP_REQUEST_NUM_THREADS);
        List<ApplicationInfo> installedApplications = applicationContext.getPackageManager()
                .getInstalledApplications(PackageManager.GET_META_DATA);
        for(final ApplicationInfo applicationInfo: installedApplications){
            final String category = PlayStoreStorage.readAppCategory(applicationContext, applicationInfo.packageName);
            if (category == null) {
                executor.execute(() -> {
                    try {
                        playStoreNetworking.cacheAppCategorySinchronously(applicationInfo.packageName, HTTP_REQUEST_TIMEOUT);
                    } catch (TimeoutException e) {
                        Utils.logWarning("Timeout exception during cache creation for:" + applicationInfo.packageName);
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        Utils.logWarning("Interrupted exception during cache creation for:" + applicationInfo.packageName);
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        Utils.logWarning("Execution exception during cache creation for:" + applicationInfo.packageName);
                        e.printStackTrace();
                    }
                });
            }
        }
        executor.shutdown();
        try {
            executor.awaitTermination(EXECUTOR_SHUTDOWN_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for(final ApplicationInfo applicationInfo: installedApplications){
            final String category = PlayStoreStorage.readAppCategory(applicationContext, applicationInfo.packageName);
            if (category == null) {
                return false;
            }
        }
        return true;

    }

}
