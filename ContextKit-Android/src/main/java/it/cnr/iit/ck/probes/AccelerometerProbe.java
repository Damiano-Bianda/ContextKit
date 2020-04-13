package it.cnr.iit.ck.probes;

import android.hardware.Sensor;

public class AccelerometerProbe extends PhysicalSensorProbe {

    @Override
    public int getDimensions() {
        return 3;
    }

    @Override
    public int getSensorId() {
        return Sensor.TYPE_ACCELEROMETER;
    }

    @Override
    public String getSensorName() {
        return "accelerometer";
    }

}
