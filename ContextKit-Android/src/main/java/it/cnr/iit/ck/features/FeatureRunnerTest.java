package it.cnr.iit.ck.features;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import it.cnr.iit.R;
import it.cnr.iit.ck.data_processing.FeatureReceiver;

public class FeatureRunnerTest implements Runnable {

    private final String SEPARATOR = ",";
    private final long timeoutInMillis;
    private final List<? extends FeatureReceiver> featureReceivers;

    private String header = "";

    private final BufferedReader datasetInputStream;

    public FeatureRunnerTest(List<? extends FeatureReceiver> featureReceivers, long timeoutInSeconds, int datasetResourceId, Context context) {
        this.datasetInputStream = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(datasetResourceId)));
        this.featureReceivers = featureReceivers;
        this.timeoutInMillis = timeoutInSeconds * 1000;
        try {
            // read header
            header = datasetInputStream.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private List<Double> buildFeaturesList(){
        List<Double> features = new ArrayList<>();
        try {
            final String row = datasetInputStream.readLine();
            if (row == null) {
                // avoid classification if there are no more rows
                features.add(Double.NaN);
            } else {
                final String[] dataStr = row.split(SEPARATOR);
                for (int i = 0; i < dataStr.length - 1; i++) {
                    features.add(Double.parseDouble(dataStr[i]));
                }
            }
        } catch (IOException e) {
            // avoid classification if there is an error during reading
            features.add(Double.NaN);
            e.printStackTrace();
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
}
