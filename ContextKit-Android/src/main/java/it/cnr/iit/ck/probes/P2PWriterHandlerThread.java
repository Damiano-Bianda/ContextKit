package it.cnr.iit.ck.probes;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import it.cnr.iit.ck.model.BTDeviceScan;
import it.cnr.iit.ck.model.BTDevices;
import it.cnr.iit.ck.model.WiFiP2PDiscoveryData;
import it.cnr.iit.ck.model.WiFiP2PDiscoveryDatas;
public class P2PWriterHandlerThread extends HandlerThread {

    private static final int INIT_MESSAGE = 1;
    private static final int SEND_P2P_ELEMENT_PARCELABLE_MESSAGE = 2;
    private static final int WRITE_PARCELABLES_MESSAGE = 3;

    private static final String PARCELABLE_KEY = "parcelable key";

    private volatile P2PWriterHandlerThread.P2PWriterHandler handler;
    private final BaseProbe probe;

    public P2PWriterHandlerThread(String name, BaseProbe probe) {
        super(name);
        this.probe = probe;
    }

    // Get a reference to worker thread's handler after looper is prepared
    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        handler = probe instanceof BLEP2PProximityProbe ?
                new P2PWriterHandlerThread.BTP2pWriterHandler(getLooper()) :
                new P2PWriterHandlerThread.WiFiP2PWriterHandler(getLooper());
    }

    public void sendP2pElement(Parcelable parcelable){
        if(handler != null) {
            Message message = handler.obtainMessage(SEND_P2P_ELEMENT_PARCELABLE_MESSAGE);
            Bundle bundle = new Bundle();
            bundle.putParcelable(PARCELABLE_KEY, parcelable);
            message.setData(bundle);
            handler.sendMessage(message);
        }
    }

    public void writeData(){
        if(handler != null) {
            Message message = handler.obtainMessage(WRITE_PARCELABLES_MESSAGE);
            handler.sendMessage(message);
        }
    }

    private static abstract class P2PWriterHandler extends Handler{

        public P2PWriterHandler(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case INIT_MESSAGE:
                    init();
                    break;
                case SEND_P2P_ELEMENT_PARCELABLE_MESSAGE:
                    handleP2pElement(msg);
                    break;
                case WRITE_PARCELABLES_MESSAGE:
                    write();
                    break;
                default:
                    break;
            }
        }

        protected abstract void init();
        public abstract void handleP2pElement(Message msg);
        public abstract void write();

    }

    private class WiFiP2PWriterHandler extends P2PWriterHandler {

        private final Set<WiFiP2PDiscoveryData> data;

        public WiFiP2PWriterHandler(Looper looper){
            super(looper);
            this.data = new HashSet<>();
        }

        public void init(){}

        @Override
        public void handleP2pElement(Message msg) {
            WiFiP2PDiscoveryData parcelable = msg.getData().getParcelable(PARCELABLE_KEY);
            data.add(parcelable);
        }

        @Override
        public void write() {
            HashSet<WiFiP2PDiscoveryData> p2PDiscoveryData = new HashSet<>(this.data);
            WiFiP2PDiscoveryDatas data = new WiFiP2PDiscoveryDatas(p2PDiscoveryData);
            probe.logOnFile(true, data);
            this.data.clear();
        }

    }


    private class BTP2pWriterHandler extends P2PWriterHandler {

        private final Set<BTDeviceScan> data;

        public BTP2pWriterHandler(Looper looper){
            super(looper);
            this.data = new HashSet<>();
        }

        @Override
        protected void init() {
        }

        @Override
        public void handleP2pElement(Message msg) {
            BTDeviceScan btDevice = msg.getData().getParcelable(PARCELABLE_KEY);
            //Log.e("Found BT Device", btDevice.getRowToLog());
            data.add(btDevice);
        }

        @Override
        public void write() {
            BTDevices btDevices = new BTDevices(new ArrayList<>(data));
            //for (String rowsToLog: btDevices.getRowsToLog())
            //    Log.e("BTDevices", rowsToLog);
            probe.post(btDevices);
            probe.logOnFile(true, btDevices);
            data.clear();
        }
    }

}