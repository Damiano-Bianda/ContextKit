package it.cnr.iit.ck.features;

import android.content.Context;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.cnr.iit.R;
import it.cnr.iit.ck.data_processing.FeatureReceiver;
import it.cnr.iit.ck.logs.FileLogger;
import it.cnr.iit.ck.probes.BaseProbe;

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

        private final Context context;
        private final List<BaseProbe> activeProbes;
        private final List<? extends FeatureReceiver> featureReceivers;

        /**
         * Create a new FeatureRunner
         */
        public FeatureRunner(List<? extends FeatureReceiver> featureReceivers, List<BaseProbe> activeProbes, Context context){
            this.featureReceivers = featureReceivers;
            this.context = context;
            this.activeProbes = activeProbes;
        }

        /**
         * Poll the message queue in order to retrieve partial feature vectors received by the probes,
         * each POLL_TIMEOUT_MILLIS the creation of a complete feature vector, is tried.
         */
        @Override
        public void run() {

            while(!Thread.currentThread().isInterrupted()){
                try {
                    Thread.sleep(timeoutInMillis);
                    List<Double> features = new ArrayList<>();
                    for (BaseProbe probe: activeProbes){
                        features.addAll(probe.getFeatures(context));
                    }
                    if (!features.contains(Double.NaN)){
                        for (FeatureReceiver receiver: featureReceivers){
                            receiver.onFeatureVectorReceived(copyToArray(features));
                        }
                    }
                    if (logfile != null) {
                        long exampleTimestamp = System.currentTimeMillis();
                        FileLogger logger = FileLogger.getInstance();
                        if (logger.logFileIsEmptyOrDoesntExists(logfile))
                            logger.store(logfile, getFeatureHeadersRow(), false);
                        logger.store(logfile, exampleTimestamp + FileLogger.SEP + TextUtils.join(FileLogger.SEP, features), false);
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

        private String getFeatureHeadersRow() {
            StringBuilder sb = new StringBuilder();
            for(String header: context.getResources().getStringArray(R.array.features_common_headers)){
                sb.append(header).append(FileLogger.SEP);
            }
            for(BaseProbe probe: activeProbes){
                String[] featureColumnNames = probe.getFeaturesHeaders();
                sb.append(TextUtils.join(FileLogger.SEP, featureColumnNames)).append(FileLogger.SEP);
            }
            return sb.substring(0, sb.length() - FileLogger.SEP.length());
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
            separator = ",";
            headersToRemove = new ArrayList<>();
            headersToRemove.add("\"time\"");
            headersToRemove.add("\"label\"");
            indicesToRemove = new HashSet<>();
            datasetInputStream = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(R.raw.context_labeler_sub_sample)));
        }

        /*
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

        }*/
    }
}