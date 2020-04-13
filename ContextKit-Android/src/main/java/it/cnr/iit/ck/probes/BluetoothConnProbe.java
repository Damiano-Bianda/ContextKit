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

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import it.cnr.iit.R;
import it.cnr.iit.ck.commons.Utils;
import it.cnr.iit.ck.model.BTDeviceConn;
import it.cnr.iit.ck.model.BTDevices;

/**
 * This probe monitors the Bluetooth connections. Reports both the name and address of each
 * connected device.
 *
 * Requires:
 *
 *  - "android.permission.BLUETOOTH"
 */
@SuppressWarnings("all")
class BluetoothConnProbe extends OnEventProbe {

    private Set<BTDeviceConn> connectedDevices = new HashSet<>();
    private int receivedProxies = 0;
    private boolean btEnabled = false;

    private final BluetoothProfile.ServiceListener initialServiceListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {

            getHandler().post(
                new Runnable() {
                    @Override
                    public void run() {
                        for(BluetoothDevice device : proxy.getConnectedDevices()){
                            connectedDevices.add(new BTDeviceConn(device, true));
                        }
                        BluetoothAdapter.getDefaultAdapter().closeProfileProxy(profile, proxy);
                        receivedProxies++;
                        if(receivedProxies == btProfiles.length) {
                            BTDevices btDevices = new BTDevices(new ArrayList(connectedDevices));
                            logOnFile(true, btDevices);
                            post(btDevices);
                        }
                    }
                }
            );

        }

        @Override
        public void onServiceDisconnected(int profile) {}
    };


    private final BroadcastReceiver connectedDevicesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if(action != null) {
                BTDeviceConn device = null;
                BluetoothDevice parcelableExtra = (BluetoothDevice) intent.getParcelableExtra(
                        BluetoothDevice.EXTRA_DEVICE);

                device = new BTDeviceConn((BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE),
                        action.equals(BluetoothDevice.ACTION_ACL_CONNECTED));

                logOnFile(true, device);
                if (device.isConnected())
                    connectedDevices.add(device);
                else
                    connectedDevices.remove(device);
                BTDevices btDevices = new BTDevices(new ArrayList(connectedDevices));
                post(btDevices);
            }
        }
    };

    private int[] btProfiles = {BluetoothProfile.A2DP, BluetoothProfile.HEADSET,
            BluetoothProfile.HEALTH};

    @Override
    public void init() {

        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        for(int btProfile : btProfiles) {
            defaultAdapter.getProfileProxy(getContext(), initialServiceListener, btProfile);


        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        getContext().registerReceiver(connectedDevicesReceiver, intentFilter, null, getHandler());
    }

    @Override
    public void onFirstRun() {
    }

    @Override
    void onStop() {
        getContext().unregisterReceiver(connectedDevicesReceiver);
    }

    @Override
    public boolean featuresData() {
        return true;
    }

    @Override
    public String[] getFeaturesHeaders() {
        return Utils.getBTFeatureHeaders("con", getContext());
    }

    @Override
    public String[] getLogFileHeaders() {
        return getContext().getResources().getStringArray(R.array.bluetooth_device_conn_log_file_headers);
    }
}
