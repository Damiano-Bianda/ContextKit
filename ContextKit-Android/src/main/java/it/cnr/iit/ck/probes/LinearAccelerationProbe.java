package it.cnr.iit.ck.probes;

import android.hardware.Sensor;

public class LinearAccelerationProbe extends PhysicalSensorProbe {
    @Override
    public int getDimensions() {
        return 3;
    }

    @Override
    public int getSensorId() {
        return Sensor.TYPE_LINEAR_ACCELERATION;
    }

    @Override
    public String getSensorName() {
        return "linear_acceleration";
    }
}
