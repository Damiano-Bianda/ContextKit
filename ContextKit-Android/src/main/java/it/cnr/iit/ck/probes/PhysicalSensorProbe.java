package it.cnr.iit.ck.probes;

import android.content.Context;
import android.hardware.SensorManager;

import java.util.List;

import it.cnr.iit.R;
import it.cnr.iit.ck.commons.Utils;
import it.cnr.iit.ck.controllers.SensorMonitor;
import it.cnr.iit.ck.model.SensorSamples;
import it.cnr.iit.ck.model.SensorsStats;

public abstract class PhysicalSensorProbe extends ContinuousProbe {

    private SensorMonitor sensorMonitor;
    private int samplingRate = getDefaultSamplingRate();

    @Override
    public void init() {
        sensorMonitor = new SensorMonitor(getContext(), this, getHandler());
    }

    @Override
    public void onFirstRun() {}

    @Override
    void onStop() {
        sensorMonitor.unRegisterSensor();
    }

    @Override
    public void exec() {
        SensorSamples sensorSamples = sensorMonitor.getSensorSamples();
        try {
            sensorSamples.padWindowWithLastElement();
        } catch (SensorSamples.CanNotPadAnInfiniteWindowException e) {
            e.printStackTrace();
        }
        List<Double> stats = sensorSamples.getStatistics();
        sensorSamples.reset();
        SensorsStats data = new SensorsStats(stats);
        logOnFile(true, data);
        post(data);
    }

    @Override
    public boolean featuresData() {
        return true;
    }

    public int getWindowSize() {
        return getInterval() * samplingRate;
    }

    public int getSamplingPeriodInMicroseconds(){
        return 1000000/samplingRate;
    }

    public boolean isSupported(){
        SensorManager sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        return sensorManager.getDefaultSensor(getSensorId()) != null;
    }

    public void setSamplingRate(int samplingRate){
        this.samplingRate = samplingRate;
    }

    public int getDefaultSamplingRate(){
        return 20;
    }

    @Override
    public String[] getFeaturesHeaders() {
        return Utils.getSensorHeaders(R.array.physical_sensor_feature_headers, false,
                getContext(), getDimensions(), getSensorName());
    }

    @Override
    public String[] getLogFileHeaders() {
        return Utils.getSensorHeaders(R.array.physical_sensor_log_file_headers, true,
                getContext(), getDimensions(), getSensorName());
    }

    /**
     * Data dimensionality of data sensor
     * @return an int that specify the data dimensionality
     */
    public abstract int getDimensions();

    public abstract int getSensorId();

    public abstract String getSensorName();

}
