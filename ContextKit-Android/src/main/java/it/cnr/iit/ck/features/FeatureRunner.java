package it.cnr.iit.ck.features;

import android.content.Context;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import it.cnr.iit.R;
import it.cnr.iit.ck.data_processing.FeatureReceiver;
import it.cnr.iit.ck.logs.FileLogger;
import it.cnr.iit.ck.probes.BaseProbe;

public class FeatureRunner implements Runnable{
    private final Context context;
    private final List<BaseProbe> activeProbes;
    private final List<? extends FeatureReceiver> featureReceivers;
    private final long timeoutInMillis;
    private final String logfile;

    /**
     * Create a new FeatureRunner
     */
    public FeatureRunner(List<? extends FeatureReceiver> featureReceivers,
                         List<BaseProbe> activeProbes,
                         long timeoutInSeconds, String logfile, Context context) {
        this.featureReceivers = featureReceivers;
        this.context = context;
        this.activeProbes = activeProbes;
        this.timeoutInMillis = timeoutInSeconds * 1000;
        this.logfile = logfile;
    }

    /**
     * Poll the message queue in order to retrieve partial feature vectors received by the probes,
     * each POLL_TIMEOUT_MILLIS the creation of a complete feature vector, is tried.
     */
    @Override
    public void run() {

        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(timeoutInMillis);
                List<Double> features = buildFeaturesList();
                if (!features.contains(Double.NaN)) {
                    for (FeatureReceiver receiver : featureReceivers) {
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

    protected List<Double> buildFeaturesList() {
        List<Double> features = new ArrayList<>();
        for (BaseProbe probe : activeProbes) {
            features.addAll(probe.getFeatures(context));
        }
        return features;
    }

    private double[] copyToArray(List<Double> features) {
        int size = features.size();
        double[] array = new double[size];
        for (int i = 0; i < size; i++) {
            array[i] = features.get(i);
        }
        return array;
    }

    private String getFeatureHeadersRow() {
        StringBuilder sb = new StringBuilder();
        for (String header : context.getResources().getStringArray(R.array.features_common_headers)) {
            sb.append(header).append(FileLogger.SEP);
        }
        for (BaseProbe probe : activeProbes) {
            String[] featureColumnNames = probe.getFeaturesHeaders();
            sb.append(TextUtils.join(FileLogger.SEP, featureColumnNames)).append(FileLogger.SEP);
        }
        return sb.substring(0, sb.length() - FileLogger.SEP.length());
    }
}
