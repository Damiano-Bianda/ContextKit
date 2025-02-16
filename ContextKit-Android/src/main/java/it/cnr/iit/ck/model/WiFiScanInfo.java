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

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import it.cnr.iit.R;
import it.cnr.iit.ck.commons.Utils;
import it.cnr.iit.ck.logs.FileLogger;
import okhttp3.internal.Util;

public class WiFiScanInfo implements MultiLoggable, Featurable{

    public List<WiFiAp> wiFiAps = new ArrayList<>();

    public void addAp(String SSID, String BSSID, int signalLevel, int dbmLevel, String capabilities,
                      int frequency, boolean connected, boolean configured){

        this.wiFiAps.add(new WiFiAp(SSID, BSSID, signalLevel, dbmLevel, capabilities, frequency,
                connected, configured));
    }

    public void addAp(WiFiAp connectedAp) {
        this.wiFiAps.add(connectedAp);
    }

    @Override
    public List<Double> getFeatures(Context context) {
        List<Double> features = new ArrayList<>();
        for(WiFiAp ap: wiFiAps){
            if (ap.isConnected()){
                features.add(1d);
                return features;
            }
        }
        features.add(0d);
        return features;
    }

    @Override
    public List<String> getRowsToLog() {
        List<String> logs = new ArrayList<>();
        for (WiFiAp wifiAp: wiFiAps) logs.add(wifiAp.getRowToLog());
        return logs;
    }

    @Override
    public boolean isEmpty() {
        return wiFiAps.isEmpty();
    }


}
