package it.cnr.iit.ck.data_classification;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import java.text.MessageFormat;

import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.SerializationHelper;

public class WekaClassifier extends CKClassifier{

    public WekaClassifier(final String classifierName, final int resourceId, int datasetInfoId, Context applicationContext) {
        super(classifierName, resourceId, datasetInfoId, applicationContext);
    }

    @Override
    public void exec() {
        this.start();
        handler = new WekaClassifierHandler(getLooper());
    }

    public class WekaClassifierHandler extends CKClassifierHandler {

        protected Classifier classifier;
        private Instances datasetDataInfo;

        public WekaClassifierHandler(Looper looper) {
            super(looper);
        }

        @Override
        protected void init() throws Exception {
            this.classifier = (Classifier) SerializationHelper.read(context.getResources().openRawResource(resourceId));
            this.datasetDataInfo = (Instances) SerializationHelper.read(context.getResources().openRawResource(datasetInfoId));
        }


        @Override
        Prediction handleDataClassification(double[] data) throws Exception {
            final int numAttributes = datasetDataInfo.numAttributes() - 1; // rimuovo classe
            if (data.length == numAttributes) {
                DenseInstance instance = new DenseInstance(numAttributes);
                datasetDataInfo.add(instance);
                instance.setDataset(datasetDataInfo);
                for (int attrIdx = 0; attrIdx < data.length; attrIdx++) {
                    instance.setValue(attrIdx, data[attrIdx]);
                }
                double predictionNumericValue = classifier.classifyInstance(instance);
                final String predictionStringLabel =
                        datasetDataInfo.classAttribute().value((int) Math.round(predictionNumericValue));
                datasetDataInfo.clear();
                return new Prediction(predictionNumericValue, predictionStringLabel);
            } else {
                return new Prediction(-1, MessageFormat.format(
                        "Model attributes length ({0}) does not match generated array length ({1})",
                        numAttributes, data.length));
            }
        }


    }

}