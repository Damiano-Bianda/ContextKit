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

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import it.cnr.iit.R;
import it.cnr.iit.ck.commons.Utils;
import it.cnr.iit.ck.data_classification.CKClassifier;
import it.cnr.iit.ck.data_classification.WekaClassifier;
import it.cnr.iit.ck.features.FeaturesWorker;
import it.cnr.iit.ck.model.SensorSamples;
import it.cnr.iit.ck.probes.BaseProbe;
import it.cnr.iit.ck.probes.ContinuousProbe;
import it.cnr.iit.ck.probes.LocationProbe;
import it.cnr.iit.ck.probes.OnEventPhysicalSensorProbe;
import it.cnr.iit.ck.probes.PhysicalSensorProbe;

/**
 * This class parses and represents the list of probes requested by the user through the
 * configuration. The configuration should be specified using the Json format.
 *
 */
class CKSetup {

    // Expected fields in the Json configuration
    private static final String JSON_FEATURES = "features";
    private static final String JSON_FEATURES_ACTIVE = "active";
    private static final String JSON_FEATURES_LOG_FILE = "logfile";
    private static final String JSON_FEATURES_TEST = "test";
    private static final String JSON_FEATURES_DATASET = "dataset";

    private static final String JSON_PROBES = "probes";
    private static final String JSON_PROBE_NAME = "name";
    private static final String JSON_PROBE_ACTIVE = "active";
    private static final String JSON_PROBE_INTERVAL = "interval";
    private static final String JSON_PROBE_START_DELAY = "startDelay";
    private static final String JSON_PROBE_LOG_FILE = "logFile";

    private static final String JSON_LOGGER_PATH = "logPath";
    private static final String JSON_REMOTE_LOGGER = "remoteLoggerDest";
    private static final String JSON_ZIPPER_INTERVAL = "zipperInterval";
    private static final String JSON_MAX_LOG_SIZE = "maxLogSizeMb";

    private static final String JSON_PHYSICAL_SENSOR_PROBE_SAMPLING = "samplingRate";

    // LocationProbe fields
    private static final boolean LOG_CATEGORIES_IN_FEATURES_DEFAULT_VALUE = true;
    private static final boolean LOG_CATEGORIES_IN_LOG_FILE = true;
    private static final String JSON_LOG_CATEGORIES_IN_FEATURES = "logCategoriesInFeatures";
    private static final String JSON_LOG_CATEGORIES_IN_LOG_FILE = "logCategoriesInLogFile";

    private static final String JSON_CLASSIFIERS = "classifiers";
    private static final String JSON_CLASSIFIER_RAW_RESOURCE = "raw_resource";
    private static final String JSON_CLASSIFIER_DATASET_INFO = "dataset_info";

    // Name of the package that contains probes
    private static final String PROBES_PKG = "it.cnr.iit.ck.probes";
    private static final String JSON_FEATURES_INTERVAL = "interval";
    public boolean featuresModuleActive;
    public String featuresLogfile;

    public List<CKClassifier> classifiers = new ArrayList<>();

    public List<BaseProbe> probes = new ArrayList<>();
    String loggerPath;
    String remoteLogger;
    Integer maxLogSizeMb;
    Integer zipperInterval;
    int featuresIntervalInSeconds;
    boolean featuresTest;
    String featuresDataset;

    private CKSetup(){}

    /**
     * Parses the Json configuration string.
     *
     * @param context       The application's context
     * @param jsonConf      The string that represents the Json configuration
     *
     * @return              The SKSetup object containing the Probe objects specified in the Json
     *                      configuration
     */
    static CKSetup parse(Context context, String jsonConf){

        CKSetup skSetup = new CKSetup();

        JSONObject conf = null;
        try {
            conf = new JSONObject(jsonConf);
        } catch (JSONException e) {
            e.printStackTrace();
            return skSetup;
        }

        try {
            JSONArray jsonClassifiers = conf.getJSONArray(JSON_CLASSIFIERS);
            for(int i = 0; i < jsonClassifiers.length(); i++){
                JSONObject jsonClassifier = jsonClassifiers.getJSONObject(i);
                String jsonClassifierRawResourceName = jsonClassifier.getString(JSON_CLASSIFIER_RAW_RESOURCE);
                int rawResourceId = context.getResources().getIdentifier(jsonClassifierRawResourceName, "raw", context.getPackageName());
                final String jsonClassifierDatasetInfoName = jsonClassifier.getString(JSON_CLASSIFIER_DATASET_INFO);
                int datasetInfoId = context.getResources().getIdentifier(jsonClassifierDatasetInfoName, "raw", context.getPackageName());
                skSetup.classifiers.add(new WekaClassifier(jsonClassifierRawResourceName, rawResourceId, datasetInfoId, context));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {

            parseFeaturesObject(skSetup, conf);

            JSONArray jsonProbes = conf.getJSONArray(JSON_PROBES);

            for(int i = 0; i < jsonProbes.length(); i++){

                String jsonObject = jsonProbes.getJSONObject(i).toString();
                BaseProbe probe = getProbeFromClass(jsonObject, context);

                if(probe != null){
                    skSetup.probes.add(probe);
                }
            }

            if(conf.has(JSON_LOGGER_PATH))
                skSetup.loggerPath = conf.getString(JSON_LOGGER_PATH).replace(
                        "\"","");

            if(conf.has(JSON_REMOTE_LOGGER))
                skSetup.remoteLogger = conf.getString(JSON_REMOTE_LOGGER).replace(
                        "\"","");

            if(conf.has(JSON_ZIPPER_INTERVAL))
                skSetup.zipperInterval = conf.getInt(JSON_ZIPPER_INTERVAL);

            if(conf.has(JSON_MAX_LOG_SIZE))
                skSetup.maxLogSizeMb = conf.getInt(JSON_MAX_LOG_SIZE);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return skSetup;
    }

    private static void parseFeaturesObject(CKSetup skSetup, JSONObject conf) throws JSONException {
        JSONObject features = conf.getJSONObject(JSON_FEATURES);

        skSetup.featuresModuleActive = features.optBoolean(JSON_FEATURES_ACTIVE, true);
        skSetup.featuresLogfile = features.optString(JSON_FEATURES_LOG_FILE, null);
        skSetup.featuresIntervalInSeconds = features.optInt(JSON_FEATURES_INTERVAL, FeaturesWorker.POLL_TIMEOUT_MILLIS);
        skSetup.featuresTest = features.optBoolean(JSON_FEATURES_TEST, false);
        skSetup.featuresDataset = features.optString(JSON_FEATURES_DATASET, null);

        if (!skSetup.featuresModuleActive && skSetup != null){
            Utils.logWarning( "Features module is deactived, no data will be logged in file " + skSetup.featuresLogfile);
        }
    }

    /**
     * Creates the Probe object based on the class name specified in the Json configuration.
     *
     * @param jsonObject        The String that represents the probe
     *
     * @param context
     * @return                  The Probe object
     */
    private static BaseProbe getProbeFromClass(String jsonObject, Context context){

        GsonBuilder builder = new GsonBuilder();
        // Needed for the the Gson library bug -----------------------------------------------------
        // https://stackoverflow.com/questions/32431279/android-m-retrofit-json-cant-make-field-
        // constructor-accessible
        builder.excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC);
        builder.serializeNulls();
        //------------------------------------------------------------------------------------------

        Gson gson = builder.create();

        JsonObject jsonProbe = new JsonParser().parse(jsonObject).getAsJsonObject();
        String className = jsonProbe.get(JSON_PROBE_NAME).getAsString();

        BaseProbe probe = null;

        try{

            Class<?> clazz = Class.forName(PROBES_PKG+"."+className);
            probe = (BaseProbe) gson.fromJson(jsonProbe, clazz);
            probe.setContext(context);

            boolean probeIsNotActive =
                    jsonProbe.has(JSON_PROBE_ACTIVE) && !jsonProbe.get(JSON_PROBE_ACTIVE).getAsBoolean();

            if(probeIsNotActive)
                return null;

            probe = parseRequiredFields(probe, jsonProbe, className);
            probe = parseOptionalFields(probe, jsonProbe);

            if (probe instanceof PhysicalSensorProbe){
                PhysicalSensorProbe sensorProbe = (PhysicalSensorProbe) probe;
                if (!probeIsSupported(sensorProbe, context) || !windowSizeIsSupported(sensorProbe, context))
                    return null;
            }

            if (probe instanceof OnEventPhysicalSensorProbe && !probeIsSupported((OnEventPhysicalSensorProbe) probe, context))
                return null;

        }catch (ClassNotFoundException e){
            e.printStackTrace();
            Log.e(Utils.TAG, "Probe "+className+" not found.");
        } catch (Exception e){
            e.printStackTrace();
            Log.e(Utils.TAG, "Error creating probe: "+e.getMessage());
        }

        return probe;
    }

    private static boolean probeIsSupported(PhysicalSensorProbe probe, Context context) {
        if (probe.isSupported())
            return true;
        Utils.logWarning(R.string.physical_sensor_not_supported_warning_message, context, probe.getClass().getSimpleName());
        return false;
    }

    private static boolean windowSizeIsSupported(PhysicalSensorProbe probe, Context context) {
        if (probe.getWindowSize() >= SensorSamples.MIN_ELEMENTS)
            return true;
        Utils.logWarning(R.string.physical_sensor_invalid_windows_size_not_supported_warning_message,
                context, probe.getClass().getSimpleName(), probe.getWindowSize(), SensorSamples.MIN_ELEMENTS);
        return false;
    }

    private static boolean probeIsSupported(OnEventPhysicalSensorProbe probe, Context context) {
        if (probe.isSupported()){
            return true;
        }
        Utils.logWarning(R.string.
                        on_event_physical_sensor_not_supported_warning_message, context,
                probe.getClass().getSimpleName());
        return false;
    }

    /**
     * Parses the probe's required parameters specified in the Json configuration, such as the
     * interval parameter required by the continuous probes.
     *
     * @param probe         The probe object
     * @param jsonObject    The JsonObject parsed from the configuration
     * @param className     Name of the probe specified in the Json configuration
     *
     * @return              the probe object with the optional parameters (if present)
     */
    private static BaseProbe parseRequiredFields(BaseProbe probe,
                                                 JsonObject jsonObject, String className){

        if(probe instanceof ContinuousProbe){

            if(!jsonObject.has(JSON_PROBE_INTERVAL)){
                Log.e(Utils.TAG, "Missing field "+JSON_PROBE_INTERVAL+" for "+className);
                probe = null;

            }else{
                ((ContinuousProbe)probe).setInterval(jsonObject.get(
                        JSON_PROBE_INTERVAL).getAsInt());
            }
        }

        return probe;
    }

    /**
     * Parses the probe's optional parameters specified in the Json configuration, such as the
     * logFile and startsDelay parameters.
     *
     * @param probe         The probe object
     * @param jsonObject    The JsonObject parsed from the configuration
     *
     * @return              the probe object with the optional parameters (if present)
     */
    private static BaseProbe parseOptionalFields(BaseProbe probe, JsonObject jsonObject){

        if(jsonObject.has(JSON_PROBE_LOG_FILE))
            probe.setLogFile(jsonObject.get(JSON_PROBE_LOG_FILE).getAsString());

        if(jsonObject.has(JSON_PROBE_START_DELAY))
            probe.setStartDelay(jsonObject.get(JSON_PROBE_START_DELAY).getAsInt());

        if(probe instanceof PhysicalSensorProbe && jsonObject.has(JSON_PHYSICAL_SENSOR_PROBE_SAMPLING)){
            ((PhysicalSensorProbe) probe).setSamplingRate(jsonObject.get(JSON_PHYSICAL_SENSOR_PROBE_SAMPLING).getAsInt());
        }

        /*

    private static final boolean LOG_CATEGORIES_IN_FEATURES_DEFAULT_VALUE = true;
    private static final boolean LOG_CATEGORIES_IN_LOG_FILE = true;
    private static final String JSON_LOG_CATEGORIES_IN_FEATURES = "logCategoriesInFeatures";
    private static final String JSON_LOG_CATEGORIES_IN_LOG_FILE = "logCategoriesInLogFile";
         */

        if(probe instanceof LocationProbe){
            LocationProbe locationProbe = (LocationProbe) probe;
            locationProbe.setLogCategoriesInFeatures(
                    jsonObject.has(JSON_LOG_CATEGORIES_IN_FEATURES) ?
                            jsonObject.get(JSON_LOG_CATEGORIES_IN_FEATURES).getAsBoolean() :
                            LOG_CATEGORIES_IN_FEATURES_DEFAULT_VALUE);
            locationProbe.setLogCategoriesInLogFile(
                    jsonObject.has(JSON_LOG_CATEGORIES_IN_LOG_FILE) ?
                            jsonObject.get(JSON_LOG_CATEGORIES_IN_LOG_FILE).getAsBoolean() :
                            LOG_CATEGORIES_IN_LOG_FILE);
        }

        return probe;
    }

}
