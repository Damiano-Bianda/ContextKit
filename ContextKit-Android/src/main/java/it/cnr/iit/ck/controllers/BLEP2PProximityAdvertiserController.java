package it.cnr.iit.ck.controllers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.util.Log;

import java.util.UUID;

import it.cnr.iit.ck.InitCKSharedPreferences;
import it.cnr.iit.ck.commons.Utils;

public class BLEP2PProximityAdvertiserController extends BLEP2PServiceController {

    private final BluetoothLeAdvertiser bluetoothLeAdvertiser;
    private final AdvertiseCallback advertisingCallback;

    private final AdvertiseSettings advertiseSettings;
    private final AdvertiseData advertiseData;

    public BLEP2PProximityAdvertiserController(boolean lowConsume, BluetoothAdapter bluetoothAdapter,
                                               int serviceId, Context applicationContext){
        bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();

        AdvertiseSettings.Builder ASBuilder = new AdvertiseSettings.Builder()
                .setConnectable(false)
                .setTimeout(0);

        if(lowConsume)
            ASBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER);
        else
            ASBuilder.setAdvertiseMode( AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                    .setTxPowerLevel( AdvertiseSettings.ADVERTISE_TX_POWER_HIGH );

        advertiseSettings = ASBuilder.build();

        advertiseData = new AdvertiseData.Builder()
                .addManufacturerData(serviceId, asBytes(UUID.fromString(InitCKSharedPreferences
                        .getUniqueDeviceID(applicationContext))))
                .build();

        advertisingCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                Log.d(Utils.TAG,  Thread.currentThread().getId() + "\tBLE device UUID advertising callback: OK");
            }

            @Override
            public void onStartFailure(int errorCode) {
                Log.e(Utils.TAG, Thread.currentThread().getId() + "\tAn error has occured during BLE advertising callback, error code (https://developer.android.com/reference/android/bluetooth/le/AdvertiseCallback): " + errorCode);
            }
        };
    }

    @Override
    public void start() {
        bluetoothLeAdvertiser.startAdvertising(advertiseSettings, advertiseData, advertisingCallback);
    }

    @Override
    public void stop() {
        bluetoothLeAdvertiser.stopAdvertising(advertisingCallback);
    }

}
