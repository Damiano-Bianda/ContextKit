package it.cnr.iit.ck.data_processing;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import java.util.List;

import it.cnr.iit.ck.data_classification.CKClassifier;

public abstract class Preprocessor extends HandlerThread {

    private static final int PREPROCESS_DATA = 0;
    private static final String DATA_KEY = "DATA_KEY";

    private final List<CKClassifier> classifiers;
    protected volatile PreprocessorHandler handler;

    public Preprocessor(final String name, final List<CKClassifier> classifiers) {
        super(name);
        this.classifiers = classifiers;
    }

    public void preprocessData(final double[] data){
        Message message = handler.obtainMessage(PREPROCESS_DATA);
        Bundle bundle = new Bundle();
        bundle.putDoubleArray(DATA_KEY, data);
        message.setData(bundle);
        handler.sendMessage(message);
    }

    public abstract void exec();

    public abstract class PreprocessorHandler extends Handler {

        public PreprocessorHandler(final Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(final Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case PREPROCESS_DATA:
                    Bundle bundle = msg.getData();
                    double[] preprocessedData = handleDataPreprocessing(bundle.getDoubleArray(DATA_KEY));
                    for (CKClassifier classifier: classifiers){
                        classifier.classifyData(preprocessedData);
                    }
                    break;
            }
        }

        protected abstract double[] handleDataPreprocessing(final double[] data);

    }
}