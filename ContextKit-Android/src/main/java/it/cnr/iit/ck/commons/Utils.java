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

package it.cnr.iit.ck.commons;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import it.cnr.iit.R;
import it.cnr.iit.ck.model.BTDevice;
import it.cnr.iit.ck.model.BTDevices;
import it.cnr.iit.ck.model.SensorSamples;

public class Utils {

    public static final String TAG = "CK";

    public static WifiManager.WifiLock acquireWifiLock(WifiManager wifiManager,
                                                        WifiManager.WifiLock wifiLock,
                                                        String lockKey) {

        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_SCAN_ONLY, lockKey);
        wifiLock.setReferenceCounted(false);
        wifiLock.acquire();

        return wifiLock;
    }

    public static WifiManager.WifiLock releaseWifiLock(WifiManager.WifiLock wifiLock) {

        if (wifiLock != null) {
            if (wifiLock.isHeld()) {
                wifiLock.release();
            }
            wifiLock = null;
        }

        return wifiLock;
    }

    public static String formatLogOutput(Object...data){

        List<String> list = new ArrayList<>();
        for(Object o : data) list.add(String.valueOf(o));

        return StringUtils.join(list, ",");
    }

    public static String formatStringForCSV(String string){
        return "\"" + string + "\"";
    }

    // OnEventPhysicalSensorProbe and PhysicalSensorProbe headers generations utils

    private static final String[] DIMENSIONS_NAMES = {"x", "y", "z", "w"};

    public static String[] getSensorHeaders(int headersResourceId, boolean withTimestamp, Context context,
                                int dimensions, String sensorName) {
        int timestamp = withTimestamp ? 1 : 0;
        String[] statisticNames = SensorSamples.STATISTIC_NAMES;
        String[] headersStringPattern = context.getResources().getStringArray(headersResourceId);
        String[] statsHeaders = new String[timestamp + dimensions * statisticNames.length];
        statsHeaders[0] = headersStringPattern[0];

        for(int dimIndex = 0; dimIndex < dimensions; dimIndex++){
            for(int statIndex = 0; statIndex < statisticNames.length; statIndex++){
                statsHeaders[timestamp + dimIndex * statisticNames.length + statIndex] =
                        String.format(headersStringPattern[timestamp], sensorName,
                                statisticNames[statIndex], DIMENSIONS_NAMES[dimIndex]);
            }
        }

        return statsHeaders;
    }

    public static String[] getBTFeatureHeaders(String probeInfo, Context context) {
        String[] headerPattern = context.getResources().getStringArray(R.array.bluetooth_feature_headers);
        String[] headers = new String[BTDevices.DEVICES_TO_FEATURIZE * BTDevice.CLASSES.length];
        for (int deviceIndex = 0; deviceIndex < BTDevices.DEVICES_TO_FEATURIZE; deviceIndex++) {
            for (int bTClassIndex = 0; bTClassIndex < BTDevice.CLASSES.length; bTClassIndex++) {
                headers[deviceIndex * BTDevice.CLASSES.length + bTClassIndex] =
                        String.format(headerPattern[0], probeInfo, ""+(deviceIndex + 1),
                                BTDevice.CLASS_NAMES.get(BTDevice.CLASSES[bTClassIndex]));
            }
        }
        return headers;
    }

    public static void logWarning(String message){
        Log.w(TAG, message);
    }

    public static void logWarning(final int resourceId, Context context){
        logWarning(context.getResources().getString(resourceId));
    }

    public static void logWarning(final int resourceId, Context context, Object... replacements){
        logWarning(String.format(context.getResources().getString(resourceId), replacements));
    }

    public static String readTextFile(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte buf[] = new byte[1024];
        int len;
        while ((len = inputStream.read(buf)) != -1) {
            outputStream.write(buf, 0, len);
        }
        outputStream.close();
        inputStream.close();
        return outputStream.toString();
    }
}
