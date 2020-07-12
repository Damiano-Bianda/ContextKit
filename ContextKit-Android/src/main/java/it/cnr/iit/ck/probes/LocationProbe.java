/*
 *   Copyright (c) 2017. Mattia Campana, mattia.campana@iit.cnr.it, Franca Delmastro, franca.delmastro@gmail.com
 *
 *   This file is part of ContextKit.
 *
 *   ContextKit (CK) is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   ContextKit (CK) is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with ContextKit (CK).  If not, see <http://www.gnu.org/licenses/>.
 */

package it.cnr.iit.ck.probes;

import android.content.res.Resources;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.grum.geocalc.Coordinate;
import com.grum.geocalc.DegreeCoordinate;
import com.grum.geocalc.EarthCalc;
import com.grum.geocalc.Point;

import org.apache.commons.lang3.ArrayUtils;

import it.cnr.iit.R;
import it.cnr.iit.ck.commons.Utils;
import it.cnr.iit.ck.controllers.VenueController;
import it.cnr.iit.ck.model.FoursquareSearchData;
import it.cnr.iit.ck.model.LocationInfo;
import it.cnr.iit.ck.model.LocationInfoEnriched;
import it.cnr.iit.ck.model.Loggable;
import it.cnr.iit.ck.model.MultiLoggable;

/**
 * This probe monitors the geographical location of the local device. Specifically, it reports the
 * following information:
 *
 *      - latitude
 *      - longitude
 *      - speed
 *      - the position's accuracy in meters
 *      - altitude
 *      - bearing
 *      - position's timestamp
 *
 * Requires:
 *
 *  - "com.google.android.gms.permission.ACCESS_COARSE_LOCATION"
 *
 */
@SuppressWarnings("unused")
public class LocationProbe extends ContinuousProbe {

    private FusedLocationProviderClient locationProviderClient;
    private VenueController venueController;
    private boolean logCategoriesInLogFile;
    private boolean logCategoriesInFeatures;

    private final VenueController.VenueListener venueListener = new VenueController.VenueListener() {
        @Override
        public void onVenueAvailable(final FoursquareSearchData foursquareSearchData, final Location location) {
            getHandler().post(() -> {
                LocationInfo locationInfo = new LocationInfo(location);
                LocationInfoEnriched locationInfoEnriched = new LocationInfoEnriched(locationInfo, foursquareSearchData);
                logAndPostData(locationInfo, locationInfoEnriched);
            });
        }

        @Override
        public void onVenueFailed(String reason, Location location) {
            getHandler().post(() -> {
                LocationInfo locationInfo = new LocationInfo(location);
                int categoriesLogHeadersCount = loadHeaders(R.array.location_categories_log_file_headers).length;
                int categoriesFeatureHeadersCount = loadHeaders(R.array.location_categories_feature_headers).length;
                LocationInfoEnriched locationInfoEnriched = new LocationInfoEnriched(locationInfo, categoriesLogHeadersCount, categoriesFeatureHeadersCount);
                logAndPostData(locationInfo, locationInfoEnriched);
            });
        }

        private void logAndPostData(LocationInfo locationInfo, LocationInfoEnriched locationInfoEnriched) {
            if (logCategoriesInLogFile && logCategoriesInFeatures) {
                logOnFile(true, locationInfoEnriched);
                setFeaturable(locationInfoEnriched);
            } else if (logCategoriesInLogFile) {
                logOnFile(true, locationInfoEnriched);
                setFeaturable(locationInfo);
            } else if (logCategoriesInFeatures) {
                logOnFile(true, locationInfo);
                setFeaturable(locationInfoEnriched);
            }
        }

    };

    @Override
    public void init() {
        locationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        venueController = new VenueController(getContext());
    }

    @Override
    public void onFirstRun() {}

    @Override
    void onStop() {
        venueController.close();
    }

    @Override
    public boolean featuresData() {
        return true;
    }

    @Override
    public String[] getFeaturesHeaders() {
        String[] baseHeaders = loadHeaders(R.array.location_feature_headers);
        return logCategoriesInFeatures ?
                ArrayUtils.addAll(baseHeaders, loadHeaders(R.array.location_categories_feature_headers)) :
                baseHeaders;
    }

    @Override
    public String[] getLogFileHeaders() {
        String[] baseHeaders = loadHeaders(R.array.location_log_file_headers);
        return logCategoriesInLogFile ?
                ArrayUtils.addAll(baseHeaders, loadHeaders(R.array.location_categories_log_file_headers)):
                baseHeaders;
    }

    private String[] loadHeaders(int id){
        return getContext().getResources().getStringArray(id);
    }

    @Override
    public void exec() {
        Task<Location> lastLocation = locationProviderClient.getLastLocation();

        if (lastLocation != null) {
            lastLocation.addOnSuccessListener(location -> {
                if (location != null) {
                    getHandler().post(
                        () -> {
                            if (logCategoriesInLogFile || logCategoriesInFeatures) {
                                venueController.getVenueByCoordinate(location, venueListener);
                            } else {
                                LocationInfo locationInfo = new LocationInfo(location);
                                logOnFile(true, locationInfo);
                                setFeaturable(locationInfo);
                            }
                        }
                    );
                }
            });
        }
    }

    public void setLogCategoriesInFeatures(boolean logCategoriesInFeatures) {
        this.logCategoriesInFeatures = logCategoriesInFeatures;
    }

    public void setLogCategoriesInLogFile(boolean logCategoriesInLogFile) {
        this.logCategoriesInLogFile = logCategoriesInLogFile;
    }

}
