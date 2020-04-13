package it.cnr.iit.ck.data_classification;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import weka.classifiers.Classifier;

public abstract class CKClassifier extends HandlerThread {

    private static final int CLASSIFY_DATA = 0;
    private static final String DATA_KEY = "DATA_KEY";

    protected final Classifier classifier;
    protected volatile CKClassifier.CKClassifierHandler handler;

    public CKClassifier(final String name, Classifier classifier) {
        super(name);
        this.classifier = classifier;
    }

    public void classifyData(final double[] data){
        Message message = handler.obtainMessage(CLASSIFY_DATA);
        Bundle bundle = new Bundle();
        bundle.putDoubleArray(DATA_KEY, data);
        message.setData(bundle);
        handler.sendMessage(message);
    }

    public abstract void exec();

    public abstract class CKClassifierHandler extends Handler {

        public CKClassifierHandler(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case CLASSIFY_DATA:
                    Bundle bundle = msg.getData();
                    try {
                        double prediction = handleDataClassification(bundle.getDoubleArray(DATA_KEY));
                        System.out.println(prediction);
                    } catch (Exception e) { e.printStackTrace(); }
                    break;
            }
        }

        abstract double handleDataClassification(final double[] data) throws Exception;

    }
}