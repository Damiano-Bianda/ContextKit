package it.cnr.iit.ck.probes;

import android.content.Context;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.Collections;
import java.util.List;

import it.cnr.iit.R;
import it.cnr.iit.ck.commons.Utils;
import it.cnr.iit.ck.controllers.SensorMonitor;
import it.cnr.iit.ck.model.Featurable;
import it.cnr.iit.ck.model.OnEventSensorData;
import it.cnr.iit.ck.model.SensorSamples;
import it.cnr.iit.ck.probes.controllers.OnEventSensorMonitor;

public abstract class OnEventPhysicalSensorProbe extends OnEventProbe {

    private SensorSamples sensorSamples;
    private SensorMonitor sensorMonitor;

    public interface OnEventListener {
        void onDataAvailable(float[] data);
    }

    private OnEventListener onSensorEventListener = data -> {
        OnEventSensorData onEventSensorData = new OnEventSensorData(data);
        logOnFile(true, onEventSensorData);
    };

    private SensorMonitor.SensorSampleEvent onSensorSampleEvent = new SensorMonitor.SensorSampleEvent() {
        @Override
        public void getSample(float[] values) {
            final OnEventSensorData data = new OnEventSensorData(values);
            logOnFile(true, data);
            setFeaturable(data);
        }
    };

    @Override
    protected synchronized void setFeaturable(Featurable featurable) {
        sensorSamples.newSample(((OnEventSensorData) featurable).getData());
    }

    @Override
    public synchronized List<Double> getFeatures(Context context) {
        if (sensorSamples == null){
            return Collections.nCopies(SensorSamples.STATISTIC_NAMES.length, Double.NaN);
        } else {
            sensorSamples.padWindowWithLastElementUntilMinQuantityOfSamples();
            final List<Double> statistics = sensorSamples.getStatistics();
            sensorSamples.reset();
            return statistics;
        }
    }

    @Override
    public void init() {
        synchronized (this){ sensorSamples = new SensorSamples(getDimensions()); }
        sensorMonitor = new SensorMonitor(getSensorId(), getDimensions(), onSensorSampleEvent,
                getContext(), getHandler());
    }

    @Override
    public void onFirstRun() {}

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
