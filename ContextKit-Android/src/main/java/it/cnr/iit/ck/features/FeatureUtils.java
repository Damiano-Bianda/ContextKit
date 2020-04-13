package it.cnr.iit.ck.features;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class FeatureUtils {

    private FeatureUtils(){}

    public static double binarize(boolean feature) {
        return feature ? 1 : 0;
    }

    /**
     * Return one hot encoding for variable in classes vector
     * @param variable
     * @param classes vector of objects (not primitives) or single values (objects or primitives)
     * @param <T>
     * @return
     */
    public static <T> List<Double> oneHotVector(T variable, T ... classes) {
        List<Double> oneHotVector = new ArrayList<>();
        for(int i = 0; i < classes.length; i++){
            oneHotVector.add(binarize(variable.equals(classes[i])));
        }
        return oneHotVector;
    }

}
