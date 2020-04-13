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
import android.os.BatteryManager;

import java.util.List;

import it.cnr.iit.R;
import it.cnr.iit.ck.logs.FileLogger;

import static it.cnr.iit.ck.features.FeatureUtils.oneHotVector;

public class BatteryInfo implements Loggable, Featurable{

    private float batteryPercentage;
    private int plugged;

    public BatteryInfo(float batteryPercentage, int plugged){

        this.plugged = plugged;
        this.batteryPercentage = batteryPercentage;
    }

    @Override
    public String getRowToLog() {
        return batteryPercentage + FileLogger.SEP + plugged;
    }

    @Override
    public List<Double> getFeatures(Context context) {
        final int NOT_PLUGGED = 0;
        List<Double> features = oneHotVector(plugged, NOT_PLUGGED, BatteryManager.BATTERY_PLUGGED_AC,
                BatteryManager.BATTERY_PLUGGED_USB, BatteryManager.BATTERY_PLUGGED_WIRELESS);
        return features;
    }

}
