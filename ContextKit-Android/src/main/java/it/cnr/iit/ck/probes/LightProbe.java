package it.cnr.iit.ck.probes;

import android.hardware.Sensor;

public class LightProbe extends PhysicalSensorProbe{
    @Override
    public int getDimensions() {
        return 1;
    }

    @Override
    public int getSensorId() {
        return Sensor.TYPE_LIGHT;
    }

    @Override
    public String getSensorName() {
        return "light";
    }
}
