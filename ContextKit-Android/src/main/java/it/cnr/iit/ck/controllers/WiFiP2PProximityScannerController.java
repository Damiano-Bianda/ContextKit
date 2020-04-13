package it.cnr.iit.ck.controllers;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Build;
import android.os.HandlerThread;
import android.util.Log;

import it.cnr.iit.ck.model.WiFiP2PDiscoveryData;
import it.cnr.iit.ck.probes.P2PWriterHandlerThread;
import it.cnr.iit.ck.probes.WiFiP2PProximityProbe;

public class WiFiP2PProximityScannerController {

    private final HandlerThread thread;

    private final WifiP2pManager.Channel channel;
    private final WifiP2pManager manager;

    private static final String UUID_DATA_KEY = "UUID_FOR_WIFI_P2P_DEVICE";

    private final WiFiP2PProximityProbe.WriterHandler writerHandler;
    private final WifiP2pDnsSdServiceRequest serviceRequest;

    public WiFiP2PProximityScannerController(final WiFiP2PProximityProbe.WriterHandler writerHandler, final Context context){
        this.writerHandler = writerHandler;
        thread = new HandlerThread("wifi p2p scanner " + System.currentTimeMillis());
        thread.start();
        this.manager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        this.channel = manager.initialize(context, thread.getLooper(), null);

        manager.setDnsSdResponseListeners(channel,
                (instanceName, registrationType, resourceType) -> {},
                (fullDomain, record, device) -> {
                    String deviceUUID = record.get(UUID_DATA_KEY);
                    WiFiP2PDiscoveryData parcelable = new WiFiP2PDiscoveryData(device, fullDomain, deviceUUID);
                    Log.e("WifiData", parcelable.getRowToLog());
                    this.writerHandler.postWifiData(parcelable);
                });

        this.serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
    }

    public void start(){
        manager.clearServiceRequests(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                manager.addServiceRequest(channel, serviceRequest, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {
                                manager.discoverServices(channel, new WifiP2pManager.ActionListener() {
                                    @Override
                                    public void onSuccess() {
                                        Log.i("WiFi Service Scanner", "Started scanning");
                                    }
                                    @Override
                                    public void onFailure(int code) {
                                        Log.e("WiFi Service Scanner", "Failed to call discoverServices, code:" + code);
                                    }
                                });
                            }
                            @Override
                            public void onFailure(int code) {
                                Log.e("WiFi Service Scanner", "Failed to call discoverPeers, code:" + code);
                            }
                        });
                    }
                    @Override
                    public void onFailure(int code) {
                        Log.e("WiFi Service Scanner", "Failed to call addServiceRequest, code:" + code);
                    }
                });
            }
            @Override
            public void onFailure(int code) {
                Log.e("WiFi Service Scanner", "Failed to call clearServiceRequests, code:" + code);
            }
        });
    }
    public void stop(){
        manager.stopPeerDiscovery(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                manager.removeServiceRequest(channel, serviceRequest, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.i("WiFi Service Scanner", "Removed service request");
                        closeResourcesAndWait();
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.e("WiFi Service Scanner", "Failed to call removeServiceRequest, code:" + reason);
                        closeResourcesAndWait();
                    }
                });
            }

            @Override
            public void onFailure(int reason) {
                Log.e("WiFi Service Scanner", "Failed to stop peer discovery, code:" + reason);
                closeResourcesAndWait();
            }
        });

    }

    private void closeResourcesAndWait() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            channel.close();
        }
        thread.quitSafely();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
