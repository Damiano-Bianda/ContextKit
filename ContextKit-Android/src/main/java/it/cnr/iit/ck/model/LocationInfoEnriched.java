package it.cnr.iit.ck.model;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import it.cnr.iit.ck.commons.Utils;
import it.cnr.iit.ck.logs.FileLogger;

public class LocationInfoEnriched implements MultiLoggable, Featurable{

    private final LocationInfo locationInfo;
    private final FoursquareSearchData foursquareSearchData;
    private int categoriesLogHeadersCount;
    private int categoriesFeatureHeadersCount;

    public LocationInfoEnriched(LocationInfo locationInfo, FoursquareSearchData foursquareSearchData){
        this.locationInfo = locationInfo;
        this.foursquareSearchData = foursquareSearchData;
    }

    public LocationInfoEnriched(LocationInfo locationInfo, int categoriesLogHeadersCount, int categoriesFeatureHeadersCount){
        this(locationInfo, null);
        this.categoriesLogHeadersCount = categoriesLogHeadersCount;
        this.categoriesFeatureHeadersCount = categoriesFeatureHeadersCount;
    }

    @Override
    public List<String> getRowsToLog() {
        String rowToLog = locationInfo.getRowToLog();
        List<String> rows = new ArrayList<>();
        if (foursquareSearchData == null) { // no foursquare answer
            rows.add(buildSingleLogRow(rowToLog, Utils.formatStringForCSV("invalid_response")));
        } else {
            List<String> foursquareRowsToLog = foursquareSearchData.getRowsToLog();
            if (foursquareRowsToLog.isEmpty()){ // foursquare answer without categories
                rows.add(buildSingleLogRow(rowToLog, Utils.formatStringForCSV("venue_without_category")));
            } else { // foursquare answer with categories
                for (String foursquareRowToLog : foursquareRowsToLog) {
                    rows.add(rowToLog + FileLogger.SEP + foursquareRowToLog);
                }
            }
        }
        return rows;
    }

    private String buildSingleLogRow(String rowToLog, String value) {
        StringBuilder sb = new StringBuilder().append(rowToLog);
        for (int i = 0; i < categoriesLogHeadersCount; i++) {
            sb.append(FileLogger.SEP).append(value);
        }
        return sb.toString();
    }

    @Override
    public boolean isEmpty() {
        return false; // return always 1 line for locationInfo
    }

    @Override
    public List<Double> getFeatures(Context context) {
        List<Double> features = locationInfo.getFeatures(context);
        if (foursquareSearchData == null){
            for (int i = 0; i < categoriesFeatureHeadersCount; i++) {
                features.add(Double.NaN);
            }
        } else {
            features.addAll(foursquareSearchData.getFeatures(context));
        }
        return features;
    }
}
