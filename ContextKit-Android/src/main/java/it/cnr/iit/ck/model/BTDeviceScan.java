package it.cnr.iit.ck.model;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.List;
import java.util.Objects;

import it.cnr.iit.ck.commons.Utils;
import it.cnr.iit.ck.logs.FileLogger;

public class BTDeviceScan extends BTDevice {

    private int rssi;
    private String uuid;

    public BTDeviceScan(ScanResult result, String uuid) {
        super(result.getDevice());
        this.rssi = result.getRssi();
        this.uuid = uuid;
    }

    public BTDeviceScan(BluetoothDevice device, short rssi, String uuid) {
        super(device);
        this.rssi = rssi;
        this.uuid = uuid;
    }

    public BTDeviceScan(Parcel in) {
        super(in);
        this.rssi = in.readInt();
        this.uuid = in.readString();
    }

    public static final Creator<BTDeviceScan> CREATOR = new Creator<BTDeviceScan>() {
        @Override
        public BTDeviceScan createFromParcel(Parcel in) {
            return new BTDeviceScan(in);
        }

        @Override
        public BTDeviceScan[] newArray(int size) {
            return new BTDeviceScan[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(rssi);
        dest.writeString(uuid);
    }

    @Override
    public String getRowToLog() {
        return super.getRowToLog() + FileLogger.SEP + rssi + FileLogger.SEP + Utils.formatStringForCSV(uuid);
    }

    @Override
    public int compareTo(@NonNull BTDevice o) {
        if (o instanceof BTDeviceScan)
            // sort by reverse order, because rssi is negative, i want strongest signal (nearest to 0) at first place
            return Integer.compare(((BTDeviceScan) o).rssi, rssi);
        return super.compareTo(o);
    }

}
