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

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import it.cnr.iit.R;
import it.cnr.iit.ck.commons.Utils;
import it.cnr.iit.ck.features.FeatureUtils;
import it.cnr.iit.ck.logs.FileLogger;

public class BTDevice implements Loggable, Featurable, Parcelable, Comparable<BTDevice>{

    public static final Integer[] CLASSES = {
            BluetoothClass.Device.Major.AUDIO_VIDEO,
            BluetoothClass.Device.Major.COMPUTER,
            BluetoothClass.Device.Major.HEALTH,
            BluetoothClass.Device.Major.IMAGING,
            BluetoothClass.Device.Major.MISC,
            BluetoothClass.Device.Major.NETWORKING,
            BluetoothClass.Device.Major.PERIPHERAL,
            BluetoothClass.Device.Major.PHONE,
            BluetoothClass.Device.Major.TOY,
            BluetoothClass.Device.Major.UNCATEGORIZED,
            BluetoothClass.Device.Major.WEARABLE};

    public static final Map<Integer, String> CLASS_NAMES;

    static {
        CLASS_NAMES = new HashMap<>();
        CLASS_NAMES.put(BluetoothClass.Device.Major.AUDIO_VIDEO,"audio_video");
        CLASS_NAMES.put(BluetoothClass.Device.Major.COMPUTER,"computer");
        CLASS_NAMES.put(BluetoothClass.Device.Major.HEALTH,"health");
        CLASS_NAMES.put(BluetoothClass.Device.Major.IMAGING,"imaging");
        CLASS_NAMES.put(BluetoothClass.Device.Major.MISC,"misc");
        CLASS_NAMES.put(BluetoothClass.Device.Major.NETWORKING,"networking");
        CLASS_NAMES.put(BluetoothClass.Device.Major.PERIPHERAL,"peripheral");
        CLASS_NAMES.put(BluetoothClass.Device.Major.PHONE,"phone");
        CLASS_NAMES.put(BluetoothClass.Device.Major.TOY,"toy");
        CLASS_NAMES.put(BluetoothClass.Device.Major.UNCATEGORIZED,"uncategorized");
        CLASS_NAMES.put(BluetoothClass.Device.Major.WEARABLE,"wearable");
    }

    protected final String name;
    protected final String address;
    protected final int majorDeviceClass;
    protected final int majorAndMinorDeviceClass;

    @SuppressLint("MissingPermission")
    public BTDevice(BluetoothDevice device){
        this.name = device.getName() == null ? "" : device.getName();
        this.address = device.getAddress();
        BluetoothClass bluetoothClass = device.getBluetoothClass();
        if (bluetoothClass == null) {
            // this is what happens when a device can not recognize class in else case
            this.majorDeviceClass = BluetoothClass.Device.Major.MISC;
            this.majorAndMinorDeviceClass = BluetoothClass.Device.Major.MISC;
        } else {
            this.majorDeviceClass = bluetoothClass.getMajorDeviceClass();
            this.majorAndMinorDeviceClass = bluetoothClass.getDeviceClass();
        }
    }

    public BTDevice(Parcel in){
        this.name = in.readString();
        this.address = in.readString();
        this.majorDeviceClass = in.readInt();
        this.majorAndMinorDeviceClass = in.readInt();
    }

    public static final Creator<BTDevice> CREATOR = new Creator<BTDevice>() {
        @Override
        public BTDevice createFromParcel(Parcel in) {
            return new BTDevice(in);
        }

        @Override
        public BTDevice[] newArray(int size) {
            return new BTDevice[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(address);
        dest.writeInt(majorDeviceClass);
        dest.writeInt(majorAndMinorDeviceClass);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BTDevice btDevice = (BTDevice) o;
        return Objects.equals(name, btDevice.name) &&
                Objects.equals(address, btDevice.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, address);
    }

    @Override
    public String getRowToLog() {
        return Utils.formatStringForCSV(name) + FileLogger.SEP + Utils.formatStringForCSV(address) +
                FileLogger.SEP + majorDeviceClass + FileLogger.SEP + majorAndMinorDeviceClass;
    }

    @Override
    public List<Double> getFeatures(Context context) {
        return FeatureUtils.oneHotVector(majorDeviceClass, CLASSES);
    }

    @Override
    public int compareTo(@NonNull BTDevice o) {
        return this.address.compareTo(o.address);
    }
}
