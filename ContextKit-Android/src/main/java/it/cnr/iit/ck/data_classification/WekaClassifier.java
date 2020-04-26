package it.cnr.iit.ck.data_classification;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import weka.classifiers.Classifier;
import weka.core.SerializationHelper;
import weka.core.SparseInstance;

public class WekaClassifier extends CKClassifier{

    public WekaClassifier(final String classifierName, final int resourceId, Context applicationContext) {
        super(classifierName, resourceId, applicationContext);
    }

    @Override
    public void exec() {
        this.start();
        handler = new WekaClassifier.DummyClassifierHandler(getLooper());
        Log.e("WekaClassifier", "exec");
    }

    public class DummyClassifierHandler extends CKClassifierHandler {

        public DummyClassifierHandler(Looper looper) {
            super(looper);
        }

        @Override
        protected void init() throws Exception {
            Log.e("WekaClassifier", "start init");
            this.classifier = (Classifier) SerializationHelper.read(context.getResources().openRawResource(resourceId));
            Log.e("WekaClassifier", "stop init");
        }

        @Override
        double handleDataClassification(double[] data) throws Exception {
            SparseInstance sparseInstance = new SparseInstance(1, data);
            double prediction = classifier.classifyInstance(sparseInstance);
            return prediction;
        }

    }
}