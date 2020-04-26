package it.cnr.iit.ck.data_processing;

public interface FeatureReceiver {
    void onFeatureVectorReceived(double[] features);
}
