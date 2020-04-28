package it.cnr.iit.ck.features;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import it.cnr.iit.R;
import it.cnr.iit.ck.commons.Utils;
import it.cnr.iit.ck.data_processing.FeatureReceiver;
import it.cnr.iit.ck.logs.FileLogger;
import it.cnr.iit.ck.model.SensorSamples;
import it.cnr.iit.ck.probes.BaseProbe;
import it.cnr.iit.ck.probes.OnEventPhysicalSensorProbe;

public class FeaturesWorker {

    public static final int POLL_TIMEOUT_MILLIS = 60;

    private final FeatureRunner featureRunner;
    private final Thread featureThread;
    private final String logfile;
    private final long timeoutInMillis;

    public FeaturesWorker(boolean featuresTest, List<? extends FeatureReceiver> featureReceivers, List<BaseProbe> probes, String logFile, long timeoutInSeconds, Context context){
        featureRunner =  featuresTest ?
                new FeatureRunnerTest(featureReceivers, probes, context) :
                new FeatureRunner(featureReceivers, probes, context);
        featureThread = new Thread(featureRunner);
        this.logfile = logFile;
        this.timeoutInMillis = timeoutInSeconds * 1000;
    }

    public void post(FeatureMessage featureMessage){
        try {
            featureRunner.messageQueue.put(featureMessage);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void start(){
        featureThread.start();
    }

    public void stop() throws InterruptedException {
        featureThread.interrupt();
        featureThread.join();
    }

    /**
     * A FeatureRunner receive partial featuresModuleActive vectors contained in FeatureMessage objects from probes
     * and maintains them.
     * Every POLL_TIMEOUT_MILLIS tries to build a complete featuresModuleActive vector.
     */
    class FeatureRunner implements Runnable{

        private final LinkedBlockingQueue<FeatureMessage> messageQueue;
        final LinkedHashMap<BaseProbe, FeatureMessage> lastMessages;
        private final HashMap<BaseProbe, FeatureMessage> defaultValues;
        final HashMap<OnEventPhysicalSensorProbe, SensorSamples> onEventSensorSamples;
        private final Context context;

        private final List<? extends FeatureReceiver> featureReceivers;

        /**
         * Create a new FeatureRunner
         * @param activeProbesNames the list with the name of active probes, order of elements is the order of construction of examples
         */
        public FeatureRunner(List<? extends FeatureReceiver> featureReceivers, List<BaseProbe> activeProbesNames, Context context){
            messageQueue = new LinkedBlockingQueue<>();
            lastMessages = new LinkedHashMap<>();
            defaultValues = new HashMap<>();
            onEventSensorSamples = new HashMap<>();
            this.featureReceivers = featureReceivers;
            this.context = context;
            FeatureMessage fakeMessage = new FeatureMessage(null, null, 0, 0);
            for(BaseProbe probe: activeProbesNames){
                lastMessages.put(probe, fakeMessage);
                if (probe instanceof  OnEventPhysicalSensorProbe){
                    OnEventPhysicalSensorProbe psp = (OnEventPhysicalSensorProbe) probe;
                    onEventSensorSamples.put(psp, new SensorSamples((psp).getDimensions()));
                }
            }
        }

        /**
         * Poll the message queue in order to retrieve partial feature vectors received by the probes,
         * each POLL_TIMEOUT_MILLIS the creation of a complete feature vector, is tried.
         */
        @Override
        public void run() {
            long timeout = timeoutInMillis;
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    long start = System.currentTimeMillis();
                    FeatureMessage message = messageQueue.poll(timeout, TimeUnit.MILLISECONDS);
                    boolean isTimeout = (message == null);
                    if(isTimeout){
                        long exampleTimestamp = System.currentTimeMillis();
                        try {
                            List<Double> features = createFeaturesVector(exampleTimestamp);
                            for (FeatureReceiver featureReceiver: featureReceivers){
                                featureReceiver.onFeatureVectorReceived(copyToArray(features));
                            }
                            if (logfile != null) {
                                FileLogger logger = FileLogger.getInstance();
                                if (logger.logFileIsEmptyOrDoesntExists(logfile))
                                    logger.store(logfile, getFeatureHeadersRow(), false);
                                logger.store(logfile, exampleTimestamp + FileLogger.SEP + TextUtils.join(FileLogger.SEP, features), false);
                            }
                        } catch (TestExampleException e) {
                            e.printStackTrace();
                        }
                        timeout = timeoutInMillis;
                    } else {
                        storeFeaturesSubvector(message);
                        long elapsed = System.currentTimeMillis() - start;
                        timeout -= elapsed;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        private double[] copyToArray(List<Double> features) {
            int size = features.size();
            double[] array = new double[size];
            for(int i = 0; i < size; i++){
                array[i] = features.get(i);
            }
            return array;
        }

        /**
         * This method generates a row containing all features header, for internal use, IT ASSUMES
         * THAT the list of valid Featurable objects are all presents, this can be assured if
         * {@link FeatureRunner#createFeaturesVector(long)} return not null with the same timestamp
         * as parameter and without modifications of this object's internal state
         * between the call of those two methods.
         * @return
         */
        private String getFeatureHeadersRow() {
            StringBuilder sb = new StringBuilder();
            for(String header: context.getResources().getStringArray(R.array.features_common_headers)){
                sb.append(header).append(FileLogger.SEP);
            }
            for(Map.Entry<BaseProbe, FeatureMessage> entry: lastMessages.entrySet()){
                BaseProbe probe = entry.getKey();
                String[] featureColumnNames = probe.getFeaturesHeaders();
                sb.append(TextUtils.join(FileLogger.SEP, featureColumnNames)).append(FileLogger.SEP);
            }
            return sb.substring(0, sb.length() - FileLogger.SEP.length());
        }

        private void storeFeaturesSubvector(FeatureMessage message) {
            BaseProbe sender = message.getSender();
            if (message.isDefaultValue()){
                defaultValues.put(sender, message);
            } else {
                lastMessages.put(sender, message);
                if (sender instanceof OnEventPhysicalSensorProbe){
                    onEventSensorSamples.get(sender).newSample(getOnEventPhysicalSensorProbeData(message));
                }
            }
        }

        private float[] getOnEventPhysicalSensorProbeData(FeatureMessage message) {
            List<Double> data = message.getFeaturableData().getFeatures(context);
            float[] convertedData = new float[data.size()];
            for(int i = 0; i < convertedData.length; i++){
                convertedData[i] = (float) ((double) data.get(i));
            }
            return convertedData;
        }

        /**
         * Concatenate all partial feature vectors in the order specified by configuration file to obtain a complete one.
         * NaN values can be contained for probes that haven't sent any data and haven't specified a default value.
         * @return a list of featuresModuleActive or null if some partial feature vectors are invalid: expired or never received by the probe
         */
        public List<Double> createFeaturesVector(long exampleTimestamp) throws TestExampleException {
            List<Double> features = new ArrayList<>();
            for(Map.Entry<BaseProbe, FeatureMessage> entry: lastMessages.entrySet()){
                BaseProbe probe = entry.getKey();
                if (probe instanceof OnEventPhysicalSensorProbe){
                    SensorSamples sensorSamples = onEventSensorSamples.get(probe);
                    sensorSamples.padWindowWithLastElementUntilMinQuantityOfSamples();
                    features.addAll(sensorSamples.getStatistics());
                    sensorSamples.reset();
                } else {
                    FeatureMessage featureMessage = entry.getValue();
                    if (featureMessage.isValid(exampleTimestamp)) {
                        features.addAll(featureMessage.getFeaturableData().getFeatures(context));
                    } else if (defaultValues.containsKey(probe)) {
                        List<Double> defaultFeatures = defaultValues.get(probe).getFeaturableData().getFeatures(context);
                        features.addAll(defaultFeatures);
                    } else {
                        features.addAll(Collections.nCopies(probe.getFeaturesHeaders().length, Double.NaN));
                    }
                }
            }
            return features;
        }

    }

    class FeatureRunnerTest extends FeatureRunner {

        private final BufferedReader datasetInputStream;
        private final String separator;
        private final List<String> headersToRemove;
        private final Set<Integer> indicesToRemove;
        private boolean readHeader = false;

        /**
         * Create a new FeatureRunner
         *
         * @param featureReceivers
         * @param activeProbesNames the list with the name of active probes, order of elements is the order of construction of examples
         * @param context
         */
        public FeatureRunnerTest(List<? extends FeatureReceiver> featureReceivers, List<BaseProbe> activeProbesNames, Context context) {
            super(featureReceivers, activeProbesNames, context);
            // TODO temp poi spostare in conf e oggetto in app
            separator = ",";
            headersToRemove = new ArrayList<>();
            headersToRemove.add("\"time\"");
            headersToRemove.add("\"label\"");
            indicesToRemove = new HashSet<>();
            datasetInputStream = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(R.raw.user_1)));
        }

        @Override
        public List<Double> createFeaturesVector(long exampleTimestamp) throws TestExampleException {
            // Avoid memory leaks
            for(Map.Entry<BaseProbe, FeatureMessage> entry: lastMessages.entrySet()) {
                BaseProbe probe = entry.getKey();
                if (probe instanceof OnEventPhysicalSensorProbe) {
                    SensorSamples sensorSamples = onEventSensorSamples.get(probe);
                    sensorSamples.reset();
                }
            }

            if (!readHeader){
                try {
                    final String headerRow = datasetInputStream.readLine();
                    final List<String> allHeaders = Arrays.asList(headerRow.split(separator));
                    for (String headerToRemove: headersToRemove){
                        final int indexToRemove = allHeaders.indexOf(headerToRemove);
                        if(indexToRemove == -1){
                            Utils.logWarning("Can not find header " + headerToRemove +
                                    " in test dataset, this data column will not be removed");
                        } else {
                            indicesToRemove.add(indexToRemove);
                        }
                    }
                    readHeader = true;
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new TestExampleException("Can not read header row, no testset will be processed");
                }
            }

            if (readHeader) {
                try {
                    final String dataRow = datasetInputStream.readLine();
                    final List<Double> features = new ArrayList<>();
                    final String[] strValues = dataRow.split(separator);
                    for (int i = 0; i < strValues.length; i++) {
                        if (!indicesToRemove.contains(i)) {
                            features.add(Double.valueOf(strValues[i]));
                        }
                    }
                    return features;
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new TestExampleException("Can not read data row, no test example will be processed");
                }
            } else {
                throw new TestExampleException("Header has not been read, no text example will be created");
            }

        }
    }
}