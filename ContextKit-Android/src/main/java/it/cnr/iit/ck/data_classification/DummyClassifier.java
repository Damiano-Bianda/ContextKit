package it.cnr.iit.ck.data_classification;

import android.os.Looper;

import weka.classifiers.Classifier;
import weka.core.SparseInstance;

public class DummyClassifier extends CKClassifier{

    public DummyClassifier(String name, Classifier classifier) {
        super(name, classifier);
    }

    @Override
    public void exec() {
        this.start();
        handler = new DummyClassifier.DummyClassifierHandler(getLooper());
    }

    public class DummyClassifierHandler extends CKClassifierHandler {

        public DummyClassifierHandler(Looper looper) {
            super(looper);
        }

        @Override
        double handleDataClassification(double[] data) throws Exception {
            SparseInstance sparseInstance = new SparseInstance(1, data);
            double prediction = classifier.classifyInstance(sparseInstance);
            return prediction;
        }

    }
}