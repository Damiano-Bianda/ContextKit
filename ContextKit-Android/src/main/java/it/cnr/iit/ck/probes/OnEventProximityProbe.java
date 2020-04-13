package it.cnr.iit.ck.probes;

import android.hardware.Sensor;

public class OnEventProximityProbe extends OnEventPhysicalSensorProbe {

    @Override
    public int getDimensions() { return 1; }

    @Override
    public int getSensorId() { return Sensor.TYPE_PROXIMITY; }

    @Override
    public String getSensorName() { return "proximity"; }

}
