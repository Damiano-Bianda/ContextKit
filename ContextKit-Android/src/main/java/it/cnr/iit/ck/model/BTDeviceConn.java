package it.cnr.iit.ck.model;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;

import it.cnr.iit.ck.logs.FileLogger;

public class BTDeviceConn extends BTDevice {

    private boolean connectionStatus;

    public BTDeviceConn(BluetoothDevice device, boolean connectionStatus) {
        super(device);
        this.connectionStatus = connectionStatus;
    }

    public BTDeviceConn(Parcel in){
        super(in);
        this.connectionStatus = (in.readInt() == 1);
    }

    public static final Creator<BTDeviceConn> CREATOR = new Creator<BTDeviceConn>() {
        @Override
        public BTDeviceConn createFromParcel(Parcel in) {
            return new BTDeviceConn(in);
        }

        @Override
        public BTDeviceConn[] newArray(int size) {
            return new BTDeviceConn[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(connectionStatus ? 1 : 0);
    }

    public Boolean isConnected() {
        return connectionStatus;
    }

    @Override
    public String getRowToLog() {
        return super.getRowToLog() + FileLogger.SEP + connectionStatus;
    }

}
