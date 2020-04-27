package it.cnr.iit.ck.data_classification;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import weka.classifiers.Classifier;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.SparseInstance;

public class WekaClassifier extends CKClassifier{

    public WekaClassifier(final String classifierName, final int resourceId, int datasetInfoId, Context applicationContext) {
        super(classifierName, resourceId, datasetInfoId, applicationContext);
    }

    @Override
    public void exec() {
        this.start();
        handler = new WekaClassifier.DummyClassifierHandler(getLooper());
        Log.e("WekaClassifier", "exec");
    }

    public class DummyClassifierHandler extends CKClassifierHandler {

        protected Classifier classifier;
        private Instances datasetDataInfo;


        public DummyClassifierHandler(Looper looper) {
            super(looper);
        }

        @Override
        protected void init() throws Exception {
            Log.e("WekaClassifier", "start init");
            this.classifier = (Classifier) SerializationHelper.read(context.getResources().openRawResource(resourceId));
            this.datasetDataInfo = (Instances) SerializationHelper.read(context.getResources().openRawResource(datasetInfoId));
            Log.e("WekaClassifier", "stop init");
        }

        @Override
        double handleDataClassification(double[] data) throws Exception {
            DenseInstance instance = new DenseInstance(datasetDataInfo.numAttributes());
            datasetDataInfo.add(instance);
            instance.setDataset(datasetDataInfo);
            for (int attrIdx = 0; attrIdx < data.length; attrIdx++){
                instance.setValue(attrIdx, data[attrIdx]);
            }
            //instance.setValue(instance.numAttributes() - 1, "test");
            //instance.setDataset(datasetDataInfo);
            double prediction = classifier.classifyInstance(instance);
            return prediction;
        }

    }
}