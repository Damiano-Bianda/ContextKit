package it.cnr.iit.ck.model;

import it.cnr.iit.ck.commons.Utils;
import it.cnr.iit.ck.logs.FileLogger;

public class WiFiAp implements Loggable{

    private String SSID, BSSID, capabilities;
    private int signalLevel, dbmLevel, frequency;
    private boolean connected, configured;

    public WiFiAp(String SSID, String BSSID, int signalLevel, int dbmLevel, String capabilities,
                  int frequency, boolean connected, boolean configured){

        this.SSID = SSID;
        this.BSSID = BSSID;
        this.signalLevel = signalLevel;
        this.dbmLevel = dbmLevel;
        this.capabilities = capabilities;
        this.frequency = frequency;
        this.connected = connected;
        this.configured = configured;
    }

    @Override
    public String getRowToLog() {
        return Utils.formatStringForCSV(SSID) + FileLogger.SEP +
                Utils.formatStringForCSV(BSSID) + FileLogger.SEP +
                signalLevel + FileLogger.SEP + dbmLevel + FileLogger.SEP +
                Utils.formatStringForCSV(capabilities) + FileLogger.SEP +
                frequency + FileLogger.SEP + connected + FileLogger.SEP + configured;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setCapabilities(String capabilities) {
        this.capabilities = capabilities;
    }
}