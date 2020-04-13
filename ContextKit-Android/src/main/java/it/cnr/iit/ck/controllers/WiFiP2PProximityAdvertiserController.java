package it.cnr.iit.ck.controllers;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import it.cnr.iit.ck.InitCKSharedPreferences;

public class WiFiP2PProximityAdvertiserController {

    private final HandlerThread thread;

    private final WifiP2pManager.Channel channel;
    private final WifiP2pManager manager;

    static final String UUID_DATA_KEY = "UUID_FOR_WIFI_P2P_DEVICE";


    private final WifiP2pDnsSdServiceInfo serviceInfo;

    public WiFiP2PProximityAdvertiserController(Context context){
        thread = new HandlerThread("wifi p2p advertiser " + System.currentTimeMillis());
        thread.start();
        this.manager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        this.channel = manager.initialize(context, thread.getLooper(), null);

        String uuid = InitCKSharedPreferences.getUniqueDeviceID(context);
        Map<String, String> record = new HashMap<>();
        record.put(UUID_DATA_KEY, uuid);
        // Service information.  Pass it an instance name, service type, _protocol._transportlayer and the map containing service infos for others devices.
        serviceInfo = WifiP2pDnsSdServiceInfo.newInstance("context_kit_" + uuid,
                "_presence._tcp", record);
    }

    public void start(){

        manager.clearLocalServices(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                manager.addLocalService(channel, serviceInfo, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.i("WiFi Service Advertiser", "Added local service");
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.e("WiFi Service Advertiser", "Failed to call addLocalService, code:" + reason);
                    }
                });
            }
            @Override
            public void onFailure(int reason) {
                Log.e("WiFi Service Advertiser", "Failed to call clearLocalServices, code:" + reason);
            }
        });
    }

    public void stop(){
        manager.removeLocalService(channel, serviceInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.e("WiFi Service Advertiser", "Removed local service");
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    channel.close();
                }
                thread.quitSafely();
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int reason) {
                Log.e("WiFi Service Advertiser", "Failed to call removeLocalService, code:" + reason);
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    channel.close();
                }
                thread.quitSafely();
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
