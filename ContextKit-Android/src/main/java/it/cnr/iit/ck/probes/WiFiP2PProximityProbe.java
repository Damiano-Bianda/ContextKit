package it.cnr.iit.ck.probes;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.HashSet;

import it.cnr.iit.R;
import it.cnr.iit.ck.controllers.WiFiP2PProximityAdvertiserController;
import it.cnr.iit.ck.controllers.WiFiP2PProximityScannerController;
import it.cnr.iit.ck.model.WiFiP2PDiscoveryData;
import it.cnr.iit.ck.model.WiFiP2PDiscoveryDatas;

public class WiFiP2PProximityProbe extends ContinuousProbe{

    private WiFiP2PProximityAdvertiserController serviceAdvertiser;
    private WiFiP2PProximityScannerController serviceDiscoverer;

    private P2PWriterHandlerThread writer;

    private boolean firstExec = true;
    private WriterHandler writerHandler;

    @Override
    public void init() {
        writer = new P2PWriterHandlerThread("wifi p2p writer", this);
        writer.start();
        writer.getLooper();
        writerHandler = new WriterHandler(getHandler().getLooper());
        serviceAdvertiser = new WiFiP2PProximityAdvertiserController(getContext());
        serviceDiscoverer = new WiFiP2PProximityScannerController(writerHandler, getContext());
    }


    @Override
    public void onFirstRun() {}

    @Override
    void onStop() {
        serviceAdvertiser.stop();
        serviceDiscoverer.stop();
    }

    @Override
    public void exec() {
        if (firstExec){
            serviceAdvertiser.start();
            serviceDiscoverer.start();
            firstExec = false;
        } else {
            serviceDiscoverer.stop();

            HashSet<WiFiP2PDiscoveryData> datas = writerHandler.getDatas();
            logOnFile(true, new WiFiP2PDiscoveryDatas(datas));
            writerHandler.clearDatas();

            serviceDiscoverer = new WiFiP2PProximityScannerController(writerHandler, getContext());
            serviceDiscoverer.start();
        }
    }

    @Override
    public boolean featuresData() {
        return false;
    }

    @Override
    public String[] getLogFileHeaders() {
        return getContext().getResources().getStringArray(R.array.wifi_p2p_service_discovery_log_file_headers);
    }

    public static class WriterHandler extends Handler {

        private final HashSet<WiFiP2PDiscoveryData> datas;

        public WriterHandler(Looper looper){
            super(looper);
            this.datas = new HashSet<>();
        }

        private static final int SEND_P2P_ELEMENT_PARCELABLE_MESSAGE = 0;
        private static final String PARCELABLE_KEY = "parcelable key";

        public void postWifiData(WiFiP2PDiscoveryData data){
            Message message = obtainMessage(SEND_P2P_ELEMENT_PARCELABLE_MESSAGE);
            Bundle bundle = new Bundle();
            bundle.putParcelable(PARCELABLE_KEY, data);
            message.setData(bundle);
            sendMessage(message);
        }

        public HashSet<WiFiP2PDiscoveryData> getDatas() {
            return datas;
        }

        public void clearDatas(){
            datas.clear();
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == SEND_P2P_ELEMENT_PARCELABLE_MESSAGE){
                WiFiP2PDiscoveryData parcelable = msg.getData().getParcelable(PARCELABLE_KEY);
                datas.add(parcelable);
            }
        }

    }
}
