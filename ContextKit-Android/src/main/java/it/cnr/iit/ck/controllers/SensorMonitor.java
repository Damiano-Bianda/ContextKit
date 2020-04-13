package it.cnr.iit.ck.controllers;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Log;

import java.util.Arrays;

import it.cnr.iit.ck.model.SensorSamples;
import it.cnr.iit.ck.probes.PhysicalSensorProbe;

public class SensorMonitor {

    private final Sensor sensor;
    private final SensorSamples sensorSamples;
    private final SensorManager sensorManager;
    private final int dataDimensionality;
    private final SensorEventListener listener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            float[] values = Arrays.copyOf(event.values, dataDimensionality);
            sensorSamples.newSample(values);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    public SensorMonitor(Context context, PhysicalSensorProbe physicalSensorProbe, Handler handler) {
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.sensor = sensorManager.getDefaultSensor(physicalSensorProbe.getSensorId());
        this.sensorSamples = new SensorSamples(physicalSensorProbe.getDimensions(), physicalSensorProbe.getWindowSize());
        this.dataDimensionality = physicalSensorProbe.getDimensions();

        sensorManager.registerListener(listener, sensor, physicalSensorProbe.getSamplingPeriodInMicroseconds(), handler);
    }

    public void unRegisterSensor() {
        sensorManager.unregisterListener(listener, sensor);
    }

    public SensorSamples getSensorSamples() {
        return sensorSamples;
    }

}
