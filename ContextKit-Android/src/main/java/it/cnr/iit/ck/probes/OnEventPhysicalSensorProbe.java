package it.cnr.iit.ck.probes;

import android.content.Context;
import android.hardware.SensorManager;
import android.util.Log;

import it.cnr.iit.R;
import it.cnr.iit.ck.commons.Utils;
import it.cnr.iit.ck.model.OnEventSensorData;
import it.cnr.iit.ck.model.SensorSamples;
import it.cnr.iit.ck.probes.controllers.OnEventSensorMonitor;

public abstract class OnEventPhysicalSensorProbe extends OnEventProbe {

    private OnEventSensorMonitor sensorMonitor;

    public interface OnEventListener {
        void onDataAvailable(float[] data);
    }

    private OnEventListener onSensorEventListener = data -> {
        OnEventSensorData onEventSensorData = new OnEventSensorData(data);
        logOnFile(true, onEventSensorData);
        post(onEventSensorData);
    };

    @Override
    public void init() {
        sensorMonitor = new OnEventSensorMonitor(getContext(), this, onSensorEventListener, getHandler());

        OnEventSensorData defaultData = OnEventSensorData.getDefaultData(getDimensions());
        postDefaultValues(defaultData);
        
    }

    @Override
    public void onFirstRun() {

    }

    @Override
    void onStop() {
        sensorMonitor.unRegisterSensor();
    }

    @Override
    public boolean featuresData() {
        return true;
    }

    @Override
    public String[] getFeaturesHeaders() {
        return Utils.getSensorHeaders(R.array.on_event_physical_sensor_feature_headers,
                false, getContext(), getDimensions(), getSensorName());
    }



    @Override
    public String[] getLogFileHeaders() {
        String[] headers = getContext().getResources().getStringArray(R.array.on_event_physical_sensor_log_file_headers);
        headers[1] = String.format(headers[1], getSensorName());
        return headers;
    }

    public boolean isSupported() {
        SensorManager sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        return sensorManager.getDefaultSensor(getSensorId()) != null;
    }

    public abstract int getDimensions();

    public abstract int getSensorId();

    public abstract String getSensorName();
}
