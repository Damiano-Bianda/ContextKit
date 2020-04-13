package it.cnr.iit.ck.probes;

import android.hardware.Sensor;

public class PressureProbe extends PhysicalSensorProbe {

    @Override
    public int getDimensions() {
        return 1;
    }

    @Override
    public int getSensorId() {
        return Sensor.TYPE_PRESSURE;
    }

    @Override
    public String getSensorName() {
        return "pressure";
    }

}
