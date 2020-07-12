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

    private static final int DEFAULT_SAMPLING_RATE = 20;

    private SensorSamples sensorSamples;
    private SensorMonitor sensorMonitor;
    private int samplingRate = DEFAULT_SAMPLING_RATE;

    private SensorMonitor.SensorSampleEvent onSensorSampleEvent = new SensorMonitor.SensorSampleEvent() {
        @Override
        public void getSample(float[] values) {
            sensorSamples.newSample(values);
        }
    };

    @Override
    public void init() {
        sensorSamples = new SensorSamples(getDimensions(), getWindowSize());
        sensorMonitor = new SensorMonitor(getSensorId(), getDimensions(),
                getSamplingPeriodInMicroseconds(), onSensorSampleEvent, getContext(), getHandler());
    }

    @Override
    public void onFirstRun() {}

    @Override
    void onStop() {
        sensorMonitor.unRegisterSensor();
    }

    @Override
    public void exec() {
        try {
            sensorSamples.padWindowWithLastElement();
        } catch (SensorSamples.CanNotPadAnInfiniteWindowException e) {
            e.printStackTrace();
        }
        List<Double> stats = sensorSamples.getStatistics();
        sensorSamples.reset();
        SensorsStats data = new SensorsStats(stats);
        logOnFile(true, data);
        setFeaturable(data);
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
