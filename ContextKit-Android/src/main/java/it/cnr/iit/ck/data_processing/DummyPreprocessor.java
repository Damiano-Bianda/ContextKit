package it.cnr.iit.ck.data_processing;

import android.os.Looper;

import java.util.List;

import it.cnr.iit.ck.data_classification.CKClassifier;
import weka.core.SparseInstance;

public class DummyPreprocessor extends Preprocessor{

    public DummyPreprocessor(final String name, final List<CKClassifier> classifiers) {
        super("DummyPreProcessorThread", classifiers);
    }

    @Override
    public void exec() {
        this.start();
        handler = new DummyPreprocessorHandler(getLooper());
    }

    public class DummyPreprocessorHandler extends Preprocessor.PreprocessorHandler{

        public DummyPreprocessorHandler(Looper looper) {
            super(looper);
        }

        @Override
        protected double[] handleDataPreprocessing(final double[] data) {
            // ...
            SparseInstance sparseInstance = new SparseInstance(1, data);
            // ...
            double[] preprocessedData = sparseInstance.toDoubleArray();
            return preprocessedData;
        }

    }
}