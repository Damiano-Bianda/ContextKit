package it.cnr.iit.ck.data_processing;

import java.util.List;

public interface FeatureReceiver {
    void onFeatureVectorReceived(List<Double> features);
}
