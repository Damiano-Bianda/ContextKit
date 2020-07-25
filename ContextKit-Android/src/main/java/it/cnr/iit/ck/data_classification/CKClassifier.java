package it.cnr.iit.ck.data_classification;

import android.content.Context;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.Arrays;

import it.cnr.iit.ck.data_processing.FeatureReceiver;
import it.cnr.iit.ck.logs.FileLogger;

public abstract class CKClassifier extends HandlerThread implements FeatureReceiver {

    private static final int CLASSIFY_DATA = 0;
    private static final String DATA_KEY = "DATA_KEY";

    final int resourceId;
    final int datasetInfoId;
    protected final Context context;
    protected volatile CKClassifier.CKClassifierHandler handler;

    public CKClassifier(final String classifierName, final int resourceId, int datasetInfoId, Context applicationContext) {
        super(classifierName + " Handler Thread");
        this.resourceId = resourceId;
        this.datasetInfoId = datasetInfoId;
        this.context = applicationContext;
    }

    @Override
    public void onFeatureVectorReceived(double[] features) {
        classifyData(features);
    }

    private void classifyData(final double[] data){
        Message message = handler.obtainMessage(CLASSIFY_DATA);
        Bundle bundle = new Bundle();
        bundle.putDoubleArray(DATA_KEY, data);
        message.setData(bundle);
        handler.sendMessage(message);
    }

    public abstract void exec();

    public abstract class CKClassifierHandler extends Handler {

        private boolean init;

        public CKClassifierHandler(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case CLASSIFY_DATA:

                    if (!init){
                        try {
                            init();
                            init = true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    Bundle bundle = msg.getData();
                    final double[] doubleArray = bundle.getDoubleArray(DATA_KEY);

                    i++;
                    try {
                        //long start = System.currentTimeMillis();
                        //long start = System.currentTimeMillis();
                        final Prediction prediction = handleDataClassification(doubleArray);
                        //long elapsed = System.currentTimeMillis() - start;

                        //FileLogger.getInstance().store("classification_times.csv", elapsed + "," + prediction.getStringLabel(), false);
                        /*
                        i++;
                        if (i % 100 == 0){
                            Log.e("class times", "logged "  + i + " examples");
                        }
                        */
                        //Log.w("predicted " + i, prediction.getStringLabel() );
                    } catch (Exception e) {
                        //e.printStackTrace();
                        //Log.e("error " + i, e.getMessage());
                    }
                    break;
            }
        }

        int i = 0;

        protected abstract void init() throws Exception;

        abstract Prediction handleDataClassification(final double[] data) throws Exception;

    }
}