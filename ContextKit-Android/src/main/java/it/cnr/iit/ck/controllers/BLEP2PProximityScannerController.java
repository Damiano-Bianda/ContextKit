package it.cnr.iit.ck.controllers;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import it.cnr.iit.ck.commons.Utils;
import it.cnr.iit.ck.model.BTDeviceScan;
import it.cnr.iit.ck.probes.P2PWriterHandlerThread;

@SuppressLint("MissingPermission")
public class BLEP2PProximityScannerController extends BLEP2PServiceController {

    private final BluetoothLeScanner bluetoothLeScanner;
    private final ScanCallback scanCallback;

    private final List<ScanFilter> scanFilters;
    private final ScanSettings scanSettings;

    private final P2PWriterHandlerThread writer;

    /**
     * Create a BLE P2P Service Scanner, it find more devices in less time if lowConsume is unset.
     * When screen is turned off some devices stop scan, a workaround is to filter scanning, this
     * is done setting compatibilityMode, when this is done only devices with same serviceId are found.
     * @param lowConsume if false scan more but increase battery usage
     * @param bluetoothAdapter device bt adapter
     * @param serviceId service to scan
     * @param applicationContext  the application context
     * @param writer thread that wrote data on file and featurize
     * @param compatibilityMode compatibility mode or not
     */
    public BLEP2PProximityScannerController(boolean lowConsume, BluetoothAdapter bluetoothAdapter,
                                            int serviceId, Context applicationContext,
                                            P2PWriterHandlerThread writer,
                                            boolean compatibilityMode){
        this.writer = writer;

        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        ScanFilter.Builder scanFilterBuilder = new ScanFilter.Builder();

        if (compatibilityMode){
            scanFilterBuilder.setManufacturerData(serviceId, new byte[0]);
        }

        scanFilters = new ArrayList<>();
        scanFilters.add(scanFilterBuilder.build());

        ScanSettings.Builder builder = new ScanSettings.Builder();

        builder.setReportDelay(0)
                .setScanMode(lowConsume? ScanSettings.SCAN_MODE_BALANCED : ScanSettings.SCAN_MODE_LOW_LATENCY);

        // with Android higher versions the scan can be more configurable and thus more efficient
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                    .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setPhy(ScanSettings.PHY_LE_ALL_SUPPORTED)
                    .setLegacy(false);
        }

        scanSettings = builder.build();

        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                byte[] manufacturerSpecificData = result.getScanRecord().getManufacturerSpecificData(serviceId);

                BTDeviceScan btDeviceScan = manufacturerSpecificData != null && manufacturerSpecificData.length == 16?
                        new BTDeviceScan(result, asUuid(manufacturerSpecificData).toString()) :
                        new BTDeviceScan(result, null);
                writer.sendP2pElement(btDeviceScan);
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                Log.d(Utils.TAG, Thread.currentThread().getId() + "\tA new BLE devices batch has been found through batch scan");
            }

            @Override
            public void onScanFailed(int errorCode) {
                Log.e(Utils.TAG, Thread.currentThread().getId() + "\tAn error has occured during BLE scanning callback, error code (https://developer.android.com/reference/android/bluetooth/le/ScanCallback): " + errorCode);
            }
        };
    }

    @Override
    public void start() {
        bluetoothLeScanner.startScan(scanFilters, scanSettings, scanCallback);
    }

    @Override
    public void stop() {
        bluetoothLeScanner.stopScan(scanCallback);
    }
}
