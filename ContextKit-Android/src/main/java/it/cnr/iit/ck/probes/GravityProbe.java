package it.cnr.iit.ck.probes;

import android.hardware.Sensor;

public class GravityProbe extends PhysicalSensorProbe {
    @Override
    public int getDimensions() {
        return 3;
    }

    @Override
    public int getSensorId() {
        return Sensor.TYPE_GRAVITY;
    }

    @Override
    public String getSensorName() {
        return "gravity";
    }

}
