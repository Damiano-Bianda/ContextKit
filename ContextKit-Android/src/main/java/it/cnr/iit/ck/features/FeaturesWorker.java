package it.cnr.iit.ck.features;

import android.content.Context;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.cnr.iit.R;
import it.cnr.iit.ck.data_processing.FeatureReceiver;
import it.cnr.iit.ck.logs.FileLogger;
import it.cnr.iit.ck.probes.BaseProbe;

public class FeaturesWorker {

    public static final int POLL_TIMEOUT_MILLIS = 60;

    private final Thread featureThread;

    public FeaturesWorker(Runnable runnable) {
        featureThread = new Thread(runnable);
    }

    public void start() {
        featureThread.start();
    }

    public void stop() throws InterruptedException {
        featureThread.interrupt();
        featureThread.join();
    }

}
