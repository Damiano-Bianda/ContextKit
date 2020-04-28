package it.cnr.iit.ck.data_classification;

public class Prediction {
    private final double numericValue;
    private final String stringLabel;

    public Prediction(double numericValue, String stringLabel) {
        this.numericValue = numericValue;
        this.stringLabel = stringLabel;
    }

    public double getNumericValue() {
        return numericValue;
    }

    public String getStringLabel() {
        return stringLabel;
    }

    @Override
    public String toString() {
        return "Prediction{" +
                "numericValue=" + numericValue +
                ", stringLabel='" + stringLabel + '\'' +
                '}';
    }

}
