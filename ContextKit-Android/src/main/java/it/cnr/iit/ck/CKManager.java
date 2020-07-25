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

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import it.cnr.iit.R;
import it.cnr.iit.ck.commons.Utils;
import it.cnr.iit.ck.data_classification.CKClassifier;
import it.cnr.iit.ck.features.FeatureRunner;
import it.cnr.iit.ck.features.FeatureRunnerTest;
import it.cnr.iit.ck.features.FeaturesWorker;
import it.cnr.iit.ck.logs.FileChecker;
import it.cnr.iit.ck.logs.FileLogger;
import it.cnr.iit.ck.logs.FileSender;
import it.cnr.iit.ck.probes.AppCategoriesUpdater;
import it.cnr.iit.ck.probes.BaseProbe;
import it.cnr.iit.ck.probes.ContinuousProbe;
import it.cnr.iit.ck.probes.OnEventProbe;
import it.cnr.iit.ck.workers.Worker;

public abstract class CKManager extends Service {

    private static final String DEVICE_FILE_NAME = "device_data.csv";

    protected List<Worker> workers = new ArrayList<>();
    protected FeaturesWorker featuresWorker;
    protected FileChecker fileChecker;

    private static final int CACHE_UPDATER_INTERVAL = 60 * 5;
    private Worker cacheUpdater;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cacheUpdater.interrupt();
        try { cacheUpdater.terminate(); } catch (InterruptedException e) { e.printStackTrace(); }
        for(Worker worker : workers) {
            try { worker.terminate(); } catch (InterruptedException e) { e.printStackTrace(); }
        }
        if(featuresWorker != null) {
            try { featuresWorker.stop(); } catch (InterruptedException e) { e.printStackTrace(); }
        }
        try { FileLogger.getInstance().processQueueAndStop(); } catch (InterruptedException e) { e.printStackTrace(); }
        if(fileChecker != null) fileChecker.stop();
    }

    protected void promoteToForegroundService() {
        Context context = getApplicationContext();
        String CHANNEL_ID = context.getString(R.string.notification_channel_id);
        int notificationId = 1;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    context.getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(context.getString(R.string.notification_channel_description));
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            Notification.Builder builder = new Notification.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.app_icon)
                    .setContentTitle(context.getString(R.string.foreground_service_notification_title))
                    .setContentText(context.getString(R.string.foreground_service_notification_text))
                    .setAutoCancel(true);

            Notification notification = builder.build();
            startForeground(notificationId, notification);
        } else {

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.app_icon)
                    .setContentTitle(context.getString(R.string.foreground_service_notification_title))
                    .setContentText(context.getString(R.string.foreground_service_notification_text))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            Notification notification = builder.build();

            startForeground(notificationId, notification);
        }
    }

    /**
     * Creates a {@link Worker} object for each Probe specified in the configuration.
     *
     * @param jsonConf      The Json configuration
     */
    protected void parseConfiguration(String jsonConf){
        new ParseTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, jsonConf);
    }

    /**
     * Reads the Json configuration. During the first execution, the configuration should be in the
     * Intent's extras. If the system kills and restart the service, the configuration can be find
     * in the shared preferences.
     *
     * @param intent    The Intent object received in the
     *                  {@link Service#onStartCommand(Intent, int, int)} method.
     *
     * @return          The Json configuration.
     */
    protected String getConfiguration(Intent intent){
        String configuration;
        if(intent != null && intent.hasExtra(CK.CONFIGURATION_INTENT_KEY)){
            configuration = intent.getStringExtra(CK.CONFIGURATION_INTENT_KEY);
        }else{
            configuration = InitCKSharedPreferences.getSavedConfiguration(this);
        }
        if(configuration != null)
            InitCKSharedPreferences.saveConfiguration(this, configuration);
        return configuration;
    }

    void startProbes(CKSetup setup){


        FileSender fileSender = null;
        if(setup.remoteLogger != null) fileSender = new FileSender(setup.remoteLogger);

        if(setup.zipperInterval != null)
            fileChecker = new FileChecker(getApplicationContext(), fileSender,
                    setup.zipperInterval, setup.maxLogSizeMb);

        for(BaseProbe probe : setup.probes) {

            Worker worker = null;

            if (probe instanceof OnEventProbe)
                worker = new Worker((OnEventProbe) probe, InitCKSharedPreferences.isFirstRun(this));

            else if (probe instanceof ContinuousProbe)
                worker = new Worker((ContinuousProbe) probe, InitCKSharedPreferences.isFirstRun(this));

            workers.add(worker);

        }

        for (Worker worker: workers) {
            worker.init();
        }

        for (Worker worker: workers) {
            worker.execute();
        }

        InitCKSharedPreferences.firstRunDone(this);
    }

    private void startFeaturesWorker(CKSetup setup) {

        if (!setup.featuresModuleActive) return;

        List<BaseProbe> featurableProbes = new ArrayList<>();
        for(BaseProbe probe: setup.probes){
            if (probe.featuresData()){
                featurableProbes.add(probe);
            }
        }

        Runnable runnable;
        if (setup.featuresTest){
            final String featuresDataset = setup.featuresDataset;
            int datasetResourceId = getApplicationContext().getResources().getIdentifier(featuresDataset, "raw", getApplicationContext().getPackageName());
            runnable = new FeatureRunnerTest(setup.classifiers, setup.featuresIntervalInSeconds, datasetResourceId, getApplicationContext());
        } else {
            runnable = new FeatureRunner(setup.classifiers, featurableProbes, setup.featuresIntervalInSeconds, setup.featuresLogfile, getApplicationContext());
        }

        featuresWorker = new FeaturesWorker(runnable);
        featuresWorker.start();


    }


    private void startAppCategoriesUpdater() {
        AppCategoriesUpdater appCategoriesUpdater = new AppCategoriesUpdater();
        appCategoriesUpdater.setContext(getApplicationContext());
        appCategoriesUpdater.setInterval(CACHE_UPDATER_INTERVAL);
        cacheUpdater = new Worker(appCategoriesUpdater, false);
        cacheUpdater.init();
        cacheUpdater.execute();
    }

    private void startFileLogger(CKSetup setup) {
        FileLogger.getInstance().init(setup.loggerPath);
        if (FileLogger.getInstance().logFileIsEmptyOrDoesntExists(DEVICE_FILE_NAME)) {
            String deviceInfosCSVFormat = InitCKSharedPreferences.
                    getDeviceInfosCSVFormat(getApplicationContext());
            if(deviceInfosCSVFormat != null) {
                FileLogger.getInstance().store(DEVICE_FILE_NAME, deviceInfosCSVFormat, false);
            }
        }
    }

    private void startClassifiers(CKSetup setup) {
        for (CKClassifier classifier: setup.classifiers){
            classifier.exec();
        }
    }

    private class ParseTask extends AsyncTask<String, Void, CKSetup>{

        @Override
        protected CKSetup doInBackground(String... conf) {
            return CKSetup.parse(getApplicationContext(), conf[0]);
        }

        @Override
        protected void onPostExecute(CKSetup setup) {
            startAppCategoriesUpdater();
            startFileLogger(setup);

            startClassifiers(setup);
            startFeaturesWorker(setup);
            startProbes(setup);
        }

    }
}
