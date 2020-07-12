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

import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.List;

import it.cnr.iit.R;
import it.cnr.iit.ck.commons.Utils;
import it.cnr.iit.ck.controllers.WeatherController;
import it.cnr.iit.ck.model.OpenWeatherData;

/**
 * This probe monitors the current weather conditions (e.g., temperature, humidity, etc.) at the
 * current device location. Weather information comes from the API service of
 * http://www.openweathermap.com
 *
 * Returns the following information:
 *
 *  - the weather conditions id, according to http://www.openweathermap.com/weather-conditions
 *  - temperature
 *  - minimum temperature
 *  - maximum temperature
 *  - humidity
 *  - pressure
 *  - wind speed
 *  - wind direction in degrees
 *  - percentage of cloudiness
 *  - rain volume for the last 3 hours
 *  - snow volume for the last 3 hour
 *  - sunrise time (unix UTC timestamp)
 *  - sunset time (unix UTC timestamp)
 *
 * Requires:
 *
 *  - "com.google.android.gms.permission.ACCESS_FINE_LOCATION"
 *  - "android.permission.INTERNET"
 *
 */
public class WeatherProbe extends ContinuousProbe {

    private FusedLocationProviderClient locationProviderClient;
    private volatile WeatherController weatherController;
    private int unit;
    private String appId;

    private final WeatherController.WeatherListener listener = new WeatherController.WeatherListener(){

        @Override
        public void onWeatherAvailable(final OpenWeatherData data) {
            getHandler().post(() -> {
                logOnFile(true, data);
                setFeaturable(data);
            });
        }

        @Override
        public void onWeatherFailed(final String reason) {
            Log.e(Utils.TAG, "Failure to receive OpenWeatherData in WeatherProbe, reason: " + reason);
        }
    };

    @Override
    public void init() {
        locationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        weatherController = new WeatherController(getContext());
    }

    @Override
    public void onFirstRun() {

    }

    @Override
    void onStop() {
        weatherController.close();
    }

    @Override
    public boolean featuresData() {
        return true;
    }

    @Override
    public String[] getFeaturesHeaders() {
        return getContext().getResources().getStringArray(R.array.weather_feature_headers);
    }

    @Override
    public String[] getLogFileHeaders() {
        return getContext().getResources().getStringArray(R.array.weather_log_file_headers);
    }

    @Override
    @SuppressWarnings("all")
    public void exec() {
        Task<Location> lastLocation = locationProviderClient.getLastLocation();
        if (lastLocation != null) {
            lastLocation.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(final Location location) {
                    if (location != null) {
                        getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                weatherController.getWeatherByCoordinates(listener,
                                        location.getLatitude(), location.getLongitude(), unit, appId);
                            }
                        });
                    }
                }
            });
        }
    }

}
