package it.cnr.iit.ck.probes.controllers;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;

import java.util.Arrays;

import it.cnr.iit.ck.model.SensorSamples;
import it.cnr.iit.ck.probes.OnEventPhysicalSensorProbe;

public class OnEventSensorMonitor {

    private final Sensor sensor;
    private final SensorManager sensorManager;
    private final int dataDimensionality;
    private final SensorSamples sensorSamples;

    private final SensorEventListener listener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            float[] values = Arrays.copyOf(event.values, dataDimensionality);
            onEventListener.onDataAvailable(values);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private final OnEventPhysicalSensorProbe.OnEventListener onEventListener;

    public OnEventSensorMonitor(Context context, OnEventPhysicalSensorProbe onEventPhysicalSensorProbe, OnEventPhysicalSensorProbe.OnEventListener onEventListener, Handler handler) {
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.sensor = sensorManager.getDefaultSensor(onEventPhysicalSensorProbe.getSensorId());
        this.sensorSamples = new SensorSamples(onEventPhysicalSensorProbe.getDimensions());
        this.dataDimensionality = onEventPhysicalSensorProbe.getDimensions();

        this.onEventListener = onEventListener;
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_FASTEST, handler);
    }

    public void unRegisterSensor() {
        sensorManager.unregisterListener(listener, sensor);
    }

    public SensorSamples getSensorSamples() {
        return sensorSamples;
    }

}
