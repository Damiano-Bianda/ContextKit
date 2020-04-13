package it.cnr.iit.ck.probes;

import android.hardware.Sensor;

public class MagneticFieldProbe extends PhysicalSensorProbe {
    @Override
    public int getDimensions() {
        return 3;
    }

    @Override
    public int getSensorId() {
        return Sensor.TYPE_MAGNETIC_FIELD;
    }

    @Override
    public String getSensorName() {
        return "magnetic_field";
    }
}
