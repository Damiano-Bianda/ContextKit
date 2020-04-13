package it.cnr.iit.ck.probes;

import android.hardware.Sensor;

public class RelativeHumidityProbe extends PhysicalSensorProbe {
    @Override
    public int getDimensions() {
        return 1;
    }

    @Override
    public int getSensorId() {
        return Sensor.TYPE_RELATIVE_HUMIDITY;
    }

    @Override
    public String getSensorName() {
        return "relative_humidity";
    }

}
