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

import android.util.Log;

import java.util.List;

import it.cnr.iit.R;
import it.cnr.iit.ck.controllers.BatteryController;
import it.cnr.iit.ck.model.BatteryInfo;
import it.cnr.iit.ck.model.Loggable;

/**
 * This probe monitors some information related to the batter.
 * Specifically, it monitors the following information:
 *  - battery level
 *  - if the device is charging
 *
 */
@SuppressWarnings("unused")
class BatteryProbe extends ContinuousProbe {

    @Override
    public void init() {}

    @Override
    public void onFirstRun() {}

    @Override
    void onStop() {}

    @Override
    public boolean featuresData() {
        return true;
    }

    @Override
    public String[] getFeaturesHeaders() {
        return getContext().getResources().getStringArray(R.array.battery_feature_headers);
    }

    @Override
    public String[] getLogFileHeaders() {
        return getContext().getResources().getStringArray(R.array.battery_log_file_headers);
    }

    @Override
    public void exec() {

        BatteryInfo batteryInfo = BatteryController.getBatteryInfo(getContext());

        if(batteryInfo != null){
            logOnFile(true, batteryInfo);
            post(batteryInfo);
        }
    }
}
