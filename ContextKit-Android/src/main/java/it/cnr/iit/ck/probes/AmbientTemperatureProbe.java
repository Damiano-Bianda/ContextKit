package it.cnr.iit.ck.probes;

import android.hardware.Sensor;

public class AmbientTemperatureProbe extends PhysicalSensorProbe{
    @Override
    public int getDimensions() {
        return 1;
    }

    @Override
    public int getSensorId() {
        return Sensor.TYPE_AMBIENT_TEMPERATURE;
    }

    @Override
    public String getSensorName() {
        return "ambient_temperature";
    }

}
