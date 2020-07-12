package it.cnr.iit.ck.probes;

import android.content.Context;
import android.net.ConnectivityManager;
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

public class WiFiProbe extends ContinuousProbe{
    private static final int MAX_RSSI_LEVELS = 4;

    private WifiManager wifiManager;
    private ConnectivityManager connManager;

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
    void onStop() {}

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

            if(!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }

            Set<String> configuredNets = getConfiguredSSID();

            WiFiScanInfo wiFiScanInfo = new WiFiScanInfo();

            WiFiAp connectedAp = getConnectedAccessPoint();
            if (connectedAp != null) {
                wiFiScanInfo.addAp(connectedAp);
                for (ScanResult result : wifiManager.getScanResults()) {
                    if (result.BSSID.equals(connectedAp.getBSSID())) {
                        connectedAp.setCapabilities(result.capabilities);
                    } else {
                        addAPToResults(configuredNets, wiFiScanInfo, result);
                    }
                }
            } else {
                for (ScanResult result : wifiManager.getScanResults()) {
                    addAPToResults(configuredNets, wiFiScanInfo, result);
                }
            }


            logOnFile(true, wiFiScanInfo);
            setFeaturable(wiFiScanInfo);

        } else {
            Log.e(Utils.TAG, "WiFiManager is null in "+this.getClass().getName());
        }
    }

    private void addAPToResults(Set<String> configuredNets, WiFiScanInfo wiFiScanInfo, ScanResult result) {
        boolean configured = configuredNets != null &&
                configuredNets.contains("\"" + result.SSID + "\"");
        wiFiScanInfo.addAp(result.SSID, result.BSSID,
                WifiManager.calculateSignalLevel(result.level, MAX_RSSI_LEVELS),
                result.level, result.capabilities, result.frequency, false,
                configured);
    }

    private WiFiAp getConnectedAccessPoint() {
        WifiInfo connectionInfo = wifiManager.getConnectionInfo();
        WiFiAp connectedAp = null;
        if(connectionInfo.getSupplicantState().equals(SupplicantState.COMPLETED)) {
            int rssi = connectionInfo.getRssi();
            connectedAp = new WiFiAp(connectionInfo.getSSID(), connectionInfo.getBSSID(),
                    WifiManager.calculateSignalLevel(rssi, MAX_RSSI_LEVELS), rssi, "[]",
                    connectionInfo.getFrequency(), true, true);
        }
        return connectedAp;
    }

    private Set<String> getConfiguredSSID() {
        Set<String> configuredNets = new HashSet<>();
        List<WifiConfiguration> configurations = wifiManager.getConfiguredNetworks();
        if(configurations != null) {
            for (WifiConfiguration conf : configurations) {
                configuredNets.add(conf.SSID);
            }
        }
        return configuredNets;
    }
}
