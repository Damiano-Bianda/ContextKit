package it.cnr.iit.ck.model;

import android.content.Context;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import it.cnr.iit.ck.logs.FileLogger;

public class OnEventSensorData implements Loggable, Featurable {

    private final float[] data;

    public  OnEventSensorData(float[] data){
        this.data = data;
    }

    @Override
    public List<Double> getFeatures(Context context) {
        List<Double> features = new ArrayList<>();
        for(float d: data)
            features.add((double) d);
        return features;
    }

    @Override
    public String getRowToLog() {
        return StringUtils.join(ArrayUtils.toObject(data), FileLogger.SEP);
    }

    public static OnEventSensorData getDefaultData(int dimensions){
        float[] data = new float[dimensions];
        for(int i = 0; i < dimensions; i++)
            data[i] = Float.NaN;
        return new OnEventSensorData(data);
    }
}
