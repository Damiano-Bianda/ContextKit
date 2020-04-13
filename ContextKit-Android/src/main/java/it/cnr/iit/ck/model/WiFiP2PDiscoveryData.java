package it.cnr.iit.ck.model;

import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

import it.cnr.iit.ck.commons.Utils;
import it.cnr.iit.ck.logs.FileLogger;

public class WiFiP2PDiscoveryData implements Loggable, Parcelable {

    private final String name;
    private final String address;
    private final String primaryDeviceType;
    private final String secondaryDeviceType;
    private final int status;
    private final String fullDomainName;
    private final String deviceUUID;

    public WiFiP2PDiscoveryData(WifiP2pDevice device, String fullDomainName, String deviceUUID){
        this.name = device.deviceName;
        this.address = device.deviceAddress;
        this.primaryDeviceType = device.primaryDeviceType;
        this.secondaryDeviceType = device.secondaryDeviceType;
        this.status = device.status;
        this.fullDomainName = fullDomainName;
        this.deviceUUID = deviceUUID;
    }

    protected WiFiP2PDiscoveryData(Parcel in) {
        this.name = in.readString();
        this.address = in.readString();
        this.primaryDeviceType = in.readString();
        this.secondaryDeviceType = in.readString();
        this.status = in.readInt();
        this.fullDomainName = in.readString();
        this.deviceUUID = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(address);
        dest.writeString(primaryDeviceType);
        dest.writeString(secondaryDeviceType);
        dest.writeInt(status);
        dest.writeString(fullDomainName);
        dest.writeString(deviceUUID);
    }

    public static final Creator<WiFiP2PDiscoveryData> CREATOR = new Creator<WiFiP2PDiscoveryData>() {
        @Override
        public WiFiP2PDiscoveryData createFromParcel(Parcel in) {
            return new WiFiP2PDiscoveryData(in);
        }

        @Override
        public WiFiP2PDiscoveryData[] newArray(int size) {
            return new WiFiP2PDiscoveryData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String getRowToLog() {
        return Utils.formatStringForCSV(name) + FileLogger.SEP + Utils.formatStringForCSV(address) +
                FileLogger.SEP + Utils.formatStringForCSV(primaryDeviceType) + FileLogger.SEP +
                Utils.formatStringForCSV(secondaryDeviceType) + FileLogger.SEP + status + FileLogger.SEP +
                Utils.formatStringForCSV(fullDomainName) + FileLogger.SEP + Utils.formatStringForCSV(deviceUUID);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WiFiP2PDiscoveryData that = (WiFiP2PDiscoveryData) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, address);
    }
}
