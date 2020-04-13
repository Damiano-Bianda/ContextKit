package it.cnr.iit.ck;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.UUID;

import it.cnr.iit.ck.commons.Utils;
import it.cnr.iit.ck.controllers.HardwareInfoController;
import it.cnr.iit.ck.logs.FileLogger;
import it.cnr.iit.ck.model.LoggableElements;

public class InitCKSharedPreferences {

    private static final String PREFS = "it.matbell.ask";

    private static final String PREF_LAST_CONFIG_KEY = "lastConfig";
    private static final String PREF_FIRST_RUN_KEY = "firstRun";
    private static final String PREF_INITIALIZED_KEY = "initialized";

    private static final String PREF_UUID_KEY = "UUID";
    private static final String ANDROID_ID_KEY = "androidId";
    private static final String DEVICE_ID_KEY = "deviceId";
    private static final String BRAND_KEY = "brand";
    private static final String MODEL_KEY = "model";
    private static final String PHONE_NUMBER_KEY = "phoneNumber";

    /**
     * Checks if this is the first time that the service has been executed.
     *
     * @return      True if this is the first execution, False otherwise.
     */
    static boolean isFirstRun(Context context){
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(
                PREF_FIRST_RUN_KEY, true);
    }

    /**
     * Sets a flag in the shared preferences to remember if the first run has been executed or not.
     */
    static void firstRunDone(Context context){
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(
                PREF_FIRST_RUN_KEY, false).apply();
    }

    public static String getUniqueDeviceID(Context context){
        return read(context, PREF_UUID_KEY);
    }

    static String getSavedConfiguration(Context context){
        return read(context, PREF_LAST_CONFIG_KEY);
    }

    static void saveConfiguration(Context context, String configuration){
        write(context, PREF_LAST_CONFIG_KEY, configuration);
    }

    static void generateFirstTimeData(Context context) {
        if(!InitCKSharedPreferences.isInitialized(context)){
            InitCKSharedPreferences.generateDeviceInfos(context);
            InitCKSharedPreferences.initialized(context);
        }
    }

    /**
     * Create a csv content file with device data
     * @param context
     * @return csv content file or null if data is not initialized
     */
    static String getDeviceInfosCSVFormat(Context context){
        if (isInitialized(context)){
            return Utils.formatStringForCSV("key") + FileLogger.SEP + Utils.formatStringForCSV("value") + "\n" +
                    Utils.formatStringForCSV(PREF_UUID_KEY) + FileLogger.SEP + Utils.formatStringForCSV(read(context, PREF_UUID_KEY)) + "\n" +
                    Utils.formatStringForCSV(ANDROID_ID_KEY) + FileLogger.SEP + Utils.formatStringForCSV(read(context, ANDROID_ID_KEY)) + "\n" +
                    Utils.formatStringForCSV(DEVICE_ID_KEY) + FileLogger.SEP + Utils.formatStringForCSV(read(context, DEVICE_ID_KEY)) + "\n" +
                    Utils.formatStringForCSV(BRAND_KEY) + FileLogger.SEP + Utils.formatStringForCSV(read(context, BRAND_KEY)) + "\n" +
                    Utils.formatStringForCSV(MODEL_KEY) + FileLogger.SEP + Utils.formatStringForCSV(read(context, MODEL_KEY)) + "\n" +
                    Utils.formatStringForCSV(PHONE_NUMBER_KEY) + FileLogger.SEP + Utils.formatStringForCSV(read(context, PHONE_NUMBER_KEY)) + "\n";
        }
        return null;
    }

    private static void generateDeviceInfos(Context context) {
        write(context, PREF_UUID_KEY, UUID.randomUUID().toString());
        write(context, ANDROID_ID_KEY, HardwareInfoController.getAndroidID(context));
        write(context, DEVICE_ID_KEY, HardwareInfoController.getDeviceID(context));
        write(context, BRAND_KEY, HardwareInfoController.getPhoneBrand());
        write(context, MODEL_KEY, HardwareInfoController.getPhoneModel());
        write(context, PHONE_NUMBER_KEY, HardwareInfoController.getPhoneNumber(context));
    }

    private static void write(Context context, String prefUuidKey, String s) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(
                prefUuidKey, s).apply();
    }

    private static String read(Context context, String prefUuidKey) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(
                prefUuidKey, null);
    }

    /**
     * Checks if CK has been already initialized by client app
     * @param context
     * @return
     */
    private static boolean isInitialized(Context context){
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(
                PREF_INITIALIZED_KEY, false);
    }

    /**
     * Sets a flag in the shared preferences to remember if CK has been initialized.
     */
    private static void initialized(Context context){
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(
                PREF_INITIALIZED_KEY, true).apply();
    }
}
