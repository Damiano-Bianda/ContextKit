package it.cnr.iit.ck.model;

import android.content.Context;

import java.util.List;

import it.cnr.iit.R;
import it.cnr.iit.ck.commons.Utils;
import it.cnr.iit.ck.features.FeatureUtils;
import it.cnr.iit.ck.logs.FileLogger;

public class TimeInfosData implements Loggable, Featurable {

    private final DayType dayType;
    private final TimeOfDay timeOfDay;

    public enum DayType{
        WEEKDAY, WEEKEND
    }

    public enum TimeOfDay {
        MORNING, AFTERNOON, EVENING, NIGHT
    }

    public TimeInfosData(DayType dayType, TimeOfDay timeOfDay){
        this.dayType = dayType;
        this.timeOfDay = timeOfDay;
    }

    @Override
    public String getRowToLog() {
        return Utils.formatStringForCSV(dayType.toString()) + FileLogger.SEP + Utils.formatStringForCSV(timeOfDay.toString());
    }

    @Override
    public List<Double> getFeatures(Context context) {
        List<Double> features = FeatureUtils.oneHotVector(dayType, DayType.values());
        features.addAll(FeatureUtils.oneHotVector(timeOfDay, TimeOfDay.values()));
        return features;
    }
}
