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

    private final SensorManager sensorManager;
    private final Sensor sensor;
    private final int dataDimensionality;
    private final SensorSampleEvent sensorSampleEvent;

    private final SensorEventListener listener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            float[] values = Arrays.copyOf(event.values, dataDimensionality);
            sensorSampleEvent.getSample(values);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private SensorMonitor(int sensorId, int dataDimensionality, SensorSampleEvent sensorSampleEvent, Context context) {
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.sensor = sensorManager.getDefaultSensor(sensorId);
        this.dataDimensionality = dataDimensionality;
        this.sensorSampleEvent = sensorSampleEvent;
    }

    public SensorMonitor(int sensorId, int dataDimensionality, int samplingPeriodInMicroseconds,
                         SensorSampleEvent sensorSampleEvent, Context context, Handler handler) {
        this(sensorId, dataDimensionality, sensorSampleEvent, context);
        sensorManager.registerListener(listener, sensor, samplingPeriodInMicroseconds, handler);
    }

    public SensorMonitor(int sensorId, int dataDimensionality, SensorSampleEvent sensorSampleEvent,
                         Context context, Handler handler) {
        this(sensorId, dataDimensionality, sensorSampleEvent, context);
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_FASTEST, handler);
    }

    public void unRegisterSensor() {
        sensorManager.unregisterListener(listener, sensor);
    }

    public interface SensorSampleEvent{
        void getSample(float[] values);
    }

}
