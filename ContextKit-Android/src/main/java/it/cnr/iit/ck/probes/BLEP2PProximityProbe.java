package it.cnr.iit.ck.probes;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.DisplayManager;
import android.util.Log;
import android.view.Display;

import it.cnr.iit.R;
import it.cnr.iit.ck.commons.Utils;
import it.cnr.iit.ck.controllers.BLEP2PProximityAdvertiserController;
import it.cnr.iit.ck.controllers.BLEP2PProximityScannerController;

import static android.view.Display.STATE_ON;

@SuppressLint("MissingPermission") // BLUETOOTH_ADMIN permission is asked by client app
public class BLEP2PProximityProbe extends ContinuousProbe {

    private static final boolean LOW_ENERGY = true;
    private static final int SERVICE_ID = 0xFFFF;

    volatile BLEP2PProximityAdvertiserController bleP2PProximityAdvertiserController;
    volatile BLEP2PProximityScannerController bleP2PProximityScannerController;
    volatile BLEP2PProximityScannerController blep2PProximityScannerControllerScreenOff;

    private P2PWriterHandlerThread writer;

    private final DisplayManager.DisplayListener displayListener = new DisplayManager.DisplayListener() {

        @Override
        public void onDisplayChanged(int displayId) {
            if (displayId == Display.DEFAULT_DISPLAY) {
                DisplayManager displayManager = (DisplayManager) getContext().getSystemService(Context.DISPLAY_SERVICE);
                Display display = displayManager.getDisplay(Display.DEFAULT_DISPLAY);
                if (display != null){
                    switch(display.getState()){
                        case STATE_ON:
                            blep2PProximityScannerControllerScreenOff.stop();
                            break;
                        default:
                            blep2PProximityScannerControllerScreenOff.start();
                            break;
                    }
                }
            }
        }

        @Override public void onDisplayAdded(int displayId) {}
        @Override public void onDisplayRemoved(int displayId) {}

    };

    private BroadcastReceiver btChecker = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())){
                int btConnectionStatus = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                switch (btConnectionStatus){
                    case BluetoothAdapter.STATE_OFF:
                        Log.e("BT status", "off");
                        BluetoothAdapter.getDefaultAdapter().enable();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.e("BT status", "turning on");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.e("BT status", "on");
                        if (bleP2PProximityAdvertiserController == null && bleP2PProximityScannerController == null && blep2PProximityScannerControllerScreenOff == null) {
                            BluetoothManager bluetoothManager = (BluetoothManager) getContext().getSystemService(Context.BLUETOOTH_SERVICE);
                            BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
                            buildControllers(bluetoothAdapter);
                        }
                        bleP2PProximityAdvertiserController.start();
                        bleP2PProximityScannerController.start();
                        blep2PProximityScannerControllerScreenOff.start();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.e("BT status", "turning off");
                        // bleP2pServiceAdvertiserController.stop();
                        // bleP2pServiceScannerController.stop();
                        break;
                    case BluetoothAdapter.ERROR:
                        break;
                }
            }
        }
    };

    @Override
    public void init() {
        BluetoothManager bluetoothManager = (BluetoothManager) getContext().getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        writer = new P2PWriterHandlerThread("bluetooth p2p writer", this);
        writer.start();

        getContext().registerReceiver(btChecker, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        if (bluetoothAdapter.isEnabled()) {
            buildControllers(bluetoothAdapter);
            bleP2PProximityAdvertiserController.start();
            bleP2PProximityScannerController.start();

            DisplayManager displayManager = (DisplayManager) getContext().getSystemService(Context.DISPLAY_SERVICE);
            Display display = displayManager.getDisplay(Display.DEFAULT_DISPLAY);
            if (display.getState() != Display.STATE_ON){
                blep2PProximityScannerControllerScreenOff.start();
            }
            observeScreenChanges(true);
        } else if (!bluetoothAdapter.enable()){
            Utils.logWarning("BTAdapter can not be activated, probe will be terminated");
            this.onStop();
        }

    }

    private void observeScreenChanges(boolean observe) {
        DisplayManager systemService = (DisplayManager) getContext().getSystemService(Context.DISPLAY_SERVICE);
        if (observe)
            systemService.registerDisplayListener(displayListener, null);
        else
            systemService.unregisterDisplayListener(displayListener);
    }

    private void buildControllers(BluetoothAdapter bluetoothAdapter) {
        bleP2PProximityAdvertiserController = new BLEP2PProximityAdvertiserController(LOW_ENERGY,
                bluetoothAdapter, SERVICE_ID, getContext());
        bleP2PProximityScannerController = new BLEP2PProximityScannerController(LOW_ENERGY,
                bluetoothAdapter, SERVICE_ID, getContext(), writer, false);
        blep2PProximityScannerControllerScreenOff = new BLEP2PProximityScannerController(LOW_ENERGY,
                bluetoothAdapter, SERVICE_ID, getContext(), writer, true);
    }


    @Override
    public void onFirstRun() { }

    @Override
    public void exec() {
        writer.writeData();
    }

    @Override
    void onStop() {

        getContext().unregisterReceiver(btChecker);
        observeScreenChanges(false);

        if (bleP2PProximityAdvertiserController != null)
            bleP2PProximityAdvertiserController.stop();

        if (bleP2PProximityScannerController != null)
            bleP2PProximityScannerController.stop();

        if (blep2PProximityScannerControllerScreenOff != null)
            blep2PProximityScannerControllerScreenOff.stop();

        if (writer != null)
            writer.quitSafely();

    }

    @Override
    public boolean featuresData() {
        return true;
    }

    @Override
    public String[] getFeaturesHeaders() {
        return Utils.getBTFeatureHeaders("proximity", getContext());
    }

    @Override
    public String[] getLogFileHeaders() {
        return getContext().getResources().getStringArray(R.array.bluetooth_device_scan_log_file_headers);
    }

    @Override
    public boolean isSupportedByDevice() {
        BluetoothManager bluetoothManager = (BluetoothManager) getContext().getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if(bluetoothAdapter == null){
            Log.d(Utils.TAG, "This device doesn't support bluetooth");
            return false;
        }
        if(!bluetoothAdapter.isEnabled()){
            bluetoothAdapter.enable();
        }
        if (bluetoothAdapter.isMultipleAdvertisementSupported()){
            Log.d(Utils.TAG, "This device doesn't support bluetooth LE advertising");
            return false;
        }
        return true;
    }
}
