package it.cnr.iit.ck.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WiFiP2PDiscoveryDatas implements MultiLoggable {

    private final Set<WiFiP2PDiscoveryData> p2PDiscoveryData;

    public WiFiP2PDiscoveryDatas(Set<WiFiP2PDiscoveryData> p2PDiscoveryData){
        this.p2PDiscoveryData = p2PDiscoveryData;
    }

    @Override
    public List<String> getRowsToLog() {
        List<String> datas = new ArrayList<>();
        for(Loggable data: p2PDiscoveryData)
            datas.add(data.getRowToLog());
        return datas;
    }

    @Override
    public boolean isEmpty() {
        return p2PDiscoveryData.isEmpty();
    }

    public void clear() {
        p2PDiscoveryData.clear();
    }

}
