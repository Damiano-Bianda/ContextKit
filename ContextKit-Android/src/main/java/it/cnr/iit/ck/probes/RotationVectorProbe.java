package it.cnr.iit.ck.probes;

import android.hardware.Sensor;

public class RotationVectorProbe extends PhysicalSensorProbe {

    @Override
    public int getDimensions() {
        return 4;
    }

    @Override
    public int getSensorId() {
        return Sensor.TYPE_ROTATION_VECTOR;
    }

    @Override
    public String getSensorName() {
        return "rotation_vector";
    }
}
