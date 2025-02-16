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

package it.cnr.iit.ck.model;

import android.content.Context;
import android.location.Location;

import java.util.ArrayList;
import java.util.List;

import it.cnr.iit.ck.logs.FileLogger;

public class LocationInfo implements Loggable, Featurable{

    private double latitude, longitude, altitude, bearing;
    private float speed, accuracy;
    private long time;

    public LocationInfo(Location location) {
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        this.speed = location.getSpeed();
        this.accuracy = location.getAccuracy();
        this.altitude = location.getAltitude();
        this.bearing = location.getBearing();
        this.time = location.getTime();
    }

    @Override
    public String getRowToLog() {
        return latitude + FileLogger.SEP + longitude + FileLogger.SEP + speed + FileLogger.SEP +
                accuracy + FileLogger.SEP + altitude + FileLogger.SEP + bearing + FileLogger.SEP +
                time;
    }

    @Override
    public List<Double> getFeatures(Context context) {
        List<Double> features = new ArrayList<>();
        features.add(latitude);
        features.add(longitude);
        features.add(altitude);
        features.add(bearing);
        features.add((double) speed);
        features.add((double) accuracy);
        features.add((double) time);
        return features;
    }

}
