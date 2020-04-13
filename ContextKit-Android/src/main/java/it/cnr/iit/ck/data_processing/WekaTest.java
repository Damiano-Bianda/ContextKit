package it.cnr.iit.ck.data_processing;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Filter;

import it.cnr.iit.R;
import it.cnr.iit.ck.commons.Utils;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.PolyKernel;
import weka.classifiers.rules.DecisionTable;
import weka.classifiers.rules.PART;
import weka.classifiers.trees.DecisionStump;
import weka.classifiers.trees.J48;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.supervised.instance.SMOTE;


public class WekaTest {
    public static BufferedReader readDataFile(String filename) {
        BufferedReader inputReader = null;

        try {
            inputReader = new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            System.err.println("File not found: " + filename);
        }

        return inputReader;
    }

    public static Evaluation classify(Classifier model,
                                      Instances trainingSet, Instances testingSet) throws Exception {
        Evaluation evaluation = new Evaluation(trainingSet);

        model.buildClassifier(trainingSet);
        evaluation.evaluateModel(model, testingSet);

        return evaluation;
    }

    public static double calculateAccuracy(FastVector predictions) {
        double correct = 0;

        for (int i = 0; i < predictions.size(); i++) {
            NominalPrediction np = (NominalPrediction) predictions.elementAt(i);
            if (np.predicted() == np.actual()) {
                correct++;
            }
        }

        return 100 * correct / predictions.size();
    }

    public static Instances[][] crossValidationSplit(Instances data, int numberOfFolds) {
        Instances[][] split = new Instances[2][numberOfFolds];

        for (int i = 0; i < numberOfFolds; i++) {
            split[0][i] = data.trainCV(numberOfFolds, i);
            split[1][i] = data.testCV(numberOfFolds, i);
        }

        return split;
    }

    public static void run2(Context context) throws IOException {
        String string = Utils.readTextFile(context.getResources().openRawResource(R.raw.weka_test));
        StringReader datafile = new StringReader(string);

        Instances dataset = new Instances(datafile);
        dataset.setClassIndex(dataset.numAttributes() - 1);

        printDatasetInfos(dataset);
        printDataset(dataset);

        Instances oversampledDataset = null;
        try {
            oversampledDataset = smote(dataset);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (oversampledDataset == null){
            System.out.println("Failed to oversample dataset");
            return;
        }

        printDatasetInfos(oversampledDataset);
        printDataset(oversampledDataset);
    }

    private static Instances smote(Instances dataset) throws Exception {
        SMOTE filter = new SMOTE();
        filter.setInputFormat(dataset);
        for (int i = 0; i < dataset.numInstances(); i++) {
            filter.input(dataset.instance(i));
        }
        filter.batchFinished();
        Instances newData = filter.getOutputFormat();
        Instance processed;
        while ((processed = filter.output()) != null) {
            newData.add(processed);
        }
        return newData;
    }

    private static void printDatasetInfos(Instances dataset) {
        Log.e("DS", "Dataset loaded");
        Log.e("DS", "Attributes: " + dataset.classAttribute());
        Log.e("DS", "Class Index: " + dataset.classIndex());
        Log.e("DS", "Number of attributes: " + dataset.numAttributes());
        Log.e("DS", "Number of classes: " + dataset.numClasses());
        Log.e("DS", "Number of instances: " + dataset.numInstances());
    }

    private static void printDataset(Instances dataset) {
        Log.e("Dataset", dataset.toString());
    }

    public static void run(Context context) throws Exception {

        String string = Utils.readTextFile(context.getResources().openRawResource(R.raw.weka_test));
        StringReader datafile = new StringReader(string);

        Instances data = new Instances(datafile);
        data.setClassIndex(data.numAttributes() - 1);

        // Do 10-split cross validation
        Instances[][] split = crossValidationSplit(data, 10);

        // Separate split into training and testing arrays
        Instances[] trainingSplits = split[0];
        Instances[] testingSplits = split[1];

        // Use a set of classifiers

        SMO smo = new SMO();
        String [] str = {"-C", "250007", "-E", "3.0"};
        PolyKernel kernel = new PolyKernel();
        kernel.setOptions(str);
        smo.setKernel(kernel);
        //smo.setOptions(str);

        Classifier[] models = {
                new J48(), // a decision tree
                new PART(),
                new DecisionTable(),//decision table majority classifier
                new DecisionStump(), //one-level decision tree
                smo
        };


        // Run for each model
        for (int j = 0; j < models.length; j++) {

            // Collect every group of predictions for current model in a FastVector
            FastVector predictions = new FastVector();

            // For each training-testing split pair, train and test the classifier
            for (int i = 0; i < trainingSplits.length; i++) {
                Evaluation validation = classify(models[j], trainingSplits[i], testingSplits[i]);

                predictions.appendElements(validation.predictions());

                // Uncomment to see the summary for each training-testing pair.
                //System.out.println(models[j].toString());
            }

            // Calculate overall accuracy of current classifier on all splits
            double accuracy = calculateAccuracy(predictions);

            // Print current classifier's name and accuracy in a complicated,
            // but nice-looking way.
            Log.e("Weka test","Accuracy of " + models[j].getClass().getSimpleName() + ": "
                    + String.format("%.2f%%", accuracy)
                    + "\n---------------------------------");
        }

    }
}