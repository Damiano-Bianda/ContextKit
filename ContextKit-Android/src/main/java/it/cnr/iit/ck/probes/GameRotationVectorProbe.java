package it.cnr.iit.ck.probes;

import android.hardware.Sensor;

public class GameRotationVectorProbe extends PhysicalSensorProbe {
    @Override
    public int getDimensions() {
        return 3;
    }

    @Override
    public int getSensorId() {
        return Sensor.TYPE_GAME_ROTATION_VECTOR;
    }

    @Override
    public String getSensorName() {
        return "game_rotation_vector";
    }
}
