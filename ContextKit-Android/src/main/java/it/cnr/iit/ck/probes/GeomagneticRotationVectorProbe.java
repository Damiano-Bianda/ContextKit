package it.cnr.iit.ck.probes;

import android.hardware.Sensor;

public class GeomagneticRotationVectorProbe extends PhysicalSensorProbe {

    @Override
    public int getDimensions() {
        return 3;
    }

    @Override
    public int getSensorId() {
        return Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR;
    }

    @Override
    public String getSensorName() {
        return "geomagnetic_rotation_vector";
    }

}
