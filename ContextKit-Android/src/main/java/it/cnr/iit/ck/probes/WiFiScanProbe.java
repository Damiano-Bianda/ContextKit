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

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.cnr.iit.R;
import it.cnr.iit.ck.commons.Utils;
import it.cnr.iit.ck.model.WiFiAp;
import it.cnr.iit.ck.model.WiFiScanInfo;

/**
 * This probe performs a continuous Wi-Fi scan to discover the Wi-Fi Access Points in proximity.
 * For each Ap, this probe reports the following information:
 *
 *      - SSID	: The AP's SSID
 *      - BSSID	: The AP's BSSID
 *      - RSSI	: The RSSI level in the [0,4] range
 *      - SIG_LEVEL	: The raw RSSI level
 *      - CAPABILITIES	: Authentication, key management, and encryption schemes supported by the AP
 *      - FREQUENCY	The primary 20 MHz frequency (in MHz) of the channel over which the client is
 *      communicating with the access point
 *      - CONNECTED	: TRUE/FALSE if the device is currently connected to the Access Point
 *      - CONFIGURED : TRUE/FALSE if the AP is in the list of already configured WIFi Networks
 *
 * Required permissions:
 *
 *      - "android.permission.ACCESS_WIFI_STATE"
 *      - "android.permission.CHANGE_WIFI_STATE"
 *      - "android.permission.ACCESS_COARSE_LOCATION"
 *      - "android.permission.ACCESS_NETWORK_STATE"
 *
 */
@SuppressWarnings("unused")
@SuppressLint("MissingPermission")
class WiFiScanProbe extends ContinuousProbe {

    private static final String LOCK_KEY = WiFiScanProbe.class.getName();
    private static final int MAX_RSSI_LEVELS = 4;

    private WifiManager wifiManager;
    private ConnectivityManager connManager;
    //private WifiManager.WifiLock wifiLock;
    private String connectedBSSID;
    private Set<String> configuredNets;

    @SuppressWarnings("all")
    private BroadcastReceiver scanResultsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {


            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {

                List<ScanResult> results = wifiManager.getScanResults();

                if (results != null) {

                    WiFiScanInfo wiFiScanInfo = new WiFiScanInfo();

                    for (ScanResult result : results) {

                        boolean connected = result.BSSID.equals(connectedBSSID);
                        boolean configured = configuredNets != null &&
                                configuredNets.contains("\""+result.SSID+"\"");

                        wiFiScanInfo.addAp(result.SSID, result.BSSID,
                                WifiManager.calculateSignalLevel(result.level, MAX_RSSI_LEVELS),
                                result.level, result.capabilities, result.frequency, connected,
                                configured);
                    }

                    logOnFile(true, wiFiScanInfo);
                    setFeaturable(wiFiScanInfo);
                }

                stop();
            }
        }
    };

    @Override
    public void init() {
        wifiManager = (WifiManager) getContext().getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);

        connManager = (ConnectivityManager) getContext().getSystemService(
                Context.CONNECTIVITY_SERVICE);
    }

    @Override
    public void onFirstRun() {}

    @Override
    @SuppressWarnings("all")
    void onStop() {
        try {
            getContext().unregisterReceiver(scanResultsReceiver);
        }catch (IllegalArgumentException ex){
            ex.printStackTrace();
        }

        connectedBSSID = null;
        configuredNets = null;
        //wifiLock = Utils.releaseWifiLock(wifiLock);
    }

    @Override
    public boolean featuresData() {
        return true;
    }

    @Override
    public String[] getFeaturesHeaders() {
        return getContext().getResources().getStringArray(R.array.wi_fi_feature_headers);
    }

    @Override
    public String[] getLogFileHeaders() {
        return getContext().getResources().getStringArray(R.array.wifi_scan_log_file_headers);
    }

    @Override
    @SuppressWarnings("all")
    public void exec() {
        if(wifiManager != null) {

            if(!wifiManager.isWifiEnabled()) wifiManager.setWifiEnabled(true);

            //wifiLock = Utils.acquireWifiLock(wifiManager, wifiLock, LOCK_KEY);

            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            configuredNets = new HashSet<>();

            List<WifiConfiguration> configurations = wifiManager.getConfiguredNetworks();
            if(configurations != null)
                for(WifiConfiguration conf : configurations){
                    configuredNets.add(conf.SSID);
                }

            if(mWifi.isConnectedOrConnecting()){
                WifiInfo wInfo = wifiManager.getConnectionInfo();
                connectedBSSID = wInfo.getBSSID();
            }

            getContext().registerReceiver(scanResultsReceiver,
                    new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION),
                    null, getHandler());

            wifiManager.startScan();

            redundanceWrite();

        } else {
            Log.e(Utils.TAG, "WiFiManager is null in "+this.getClass().getName());
        }
    }

    // This method is useful because some phones (s8) didn't receive intent so often, thus we
    // log some data about AP that can be not so updated as those in BCReceiver, but we update
    // features with updated connected AP
    private void redundanceWrite() {
        WifiInfo connectionInfo = wifiManager.getConnectionInfo();

        WiFiScanInfo wiFiScanInfo = new WiFiScanInfo();

        WiFiAp connectedAp = null;

        // necessary construction because some time AP can be connected but no in scanned list if
        // done outside broadcast receiver
        final boolean isEffectivelyConnected = connectionInfo.getSupplicantState().equals(SupplicantState.COMPLETED);
        if(isEffectivelyConnected) {
            int rssi = connectionInfo.getRssi();
            connectedAp = new WiFiAp(connectionInfo.getSSID(), connectionInfo.getBSSID(),
                    WifiManager.calculateSignalLevel(rssi, MAX_RSSI_LEVELS), rssi, "[]",
                    connectionInfo.getFrequency(), true, true);
        }

        for (ScanResult result : wifiManager.getScanResults()) {

            boolean isConnectedAp = result.BSSID.equals(connectedBSSID);
            if(isConnectedAp && isEffectivelyConnected) {
                connectedAp.setCapabilities(result.capabilities);
            } else {
                boolean configured = configuredNets != null &&
                        configuredNets.contains("\"" + result.SSID + "\"");
                wiFiScanInfo.addAp(result.SSID, result.BSSID,
                        WifiManager.calculateSignalLevel(result.level, MAX_RSSI_LEVELS),
                        result.level, result.capabilities, result.frequency, false,
                        configured);
            }
        }

        if (isEffectivelyConnected){
            wiFiScanInfo.addAp(connectedAp);
        }

        logOnRedundantFile(true, wiFiScanInfo);
        setFeaturable(wiFiScanInfo);
    }
}
