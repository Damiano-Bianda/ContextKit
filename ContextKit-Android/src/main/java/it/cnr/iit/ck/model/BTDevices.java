package it.cnr.iit.ck.model;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class BTDevices implements Featurable, MultiLoggable{

    public static final int DEVICES_TO_FEATURIZE = 5;

    private List<BTDevice> devices;

    public BTDevices(List<BTDevice> devices) {
        // do not use subList method because throw Exception, because of structural changes of Collections.sort().
        //this.devices = devices.subList(0, Math.min(devices.size(), DEVICES_TO_FEATURIZE));
        Collections.sort(devices);
        this.devices = new ArrayList<>();
        for(int i = 0; i < Math.min(devices.size(), DEVICES_TO_FEATURIZE); i++)
            this.devices.add(i, devices.get(i));
    }

    @Override
    public List<Double> getFeatures(Context context) {

        List<Double> features = new ArrayList<>();
        for(int i = 0; i < devices.size(); i++)
            features.addAll(devices.get(i).getFeatures(context));

        int totalElements = DEVICES_TO_FEATURIZE * BTDevice.CLASSES.length;
        int currentSize = features.size();
        if(currentSize < totalElements)
            features.addAll(new ArrayList<Double>(
                    Collections.nCopies(totalElements - currentSize, 0.0d)));

        return features;
    }

    @Override
    public List<String> getRowsToLog() {
        List<String> data = new ArrayList<>();
        for (BTDevice device : devices) data.add(device.getRowToLog());
        return data;
    }

    @Override
    public boolean isEmpty() {
        return devices.isEmpty();
    }
}
