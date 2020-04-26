package it.cnr.iit.ck.data_classification;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import it.cnr.iit.ck.data_processing.FeatureReceiver;
import weka.classifiers.Classifier;

public abstract class CKClassifier extends HandlerThread implements FeatureReceiver {

    private static final int CLASSIFY_DATA = 0;
    private static final String DATA_KEY = "DATA_KEY";

    protected final int resourceId;
    protected final Context context;
    protected volatile CKClassifier.CKClassifierHandler handler;

    public CKClassifier(final String classifierName, final int resourceId, Context applicationContext) {
        super(classifierName + " Handler Thread");
        this.resourceId = resourceId;
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
        protected Classifier classifier;

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
                            // TODO terminare e avvisare threads sottostanti
                        }
                    }

                    Bundle bundle = msg.getData();
                    try {
                        // TODO aggiungere info dataset!
                        double prediction = handleDataClassification(bundle.getDoubleArray(DATA_KEY));
                        System.out.println(prediction);
                    } catch (Exception e) { e.printStackTrace(); }
                    break;
            }
        }

        protected abstract void init() throws Exception;

        abstract double handleDataClassification(final double[] data) throws Exception;

    }
}