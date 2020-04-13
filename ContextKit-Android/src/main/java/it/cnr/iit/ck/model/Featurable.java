package it.cnr.iit.ck.model;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public interface Featurable {
    List<Double> getFeatures(Context context);

}
