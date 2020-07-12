package it.cnr.iit.ck.probes;

import android.util.Log;

import java.util.List;

import it.cnr.iit.R;
import it.cnr.iit.ck.controllers.TimeInfosController;
import it.cnr.iit.ck.model.TimeInfosData;

public class TimeInfosProbe extends ContinuousProbe{
    @Override
    public void init() {

    }

    @Override
    public void onFirstRun() {

    }

    @Override
    void onStop() {

    }

    @Override
    public boolean featuresData() {
        return true;
    }

    @Override
    public String[] getFeaturesHeaders() {
        return getContext().getResources().getStringArray(R.array.time_infos_feature_headers);
    }

    @Override
    public String[] getLogFileHeaders() {
        return getContext().getResources().getStringArray(R.array.time_infos_data_log_file_headers);
    }

    @Override
    public void exec() {
        TimeInfosData timeInfosData = TimeInfosController.getTimeInfosData();
        logOnFile(true, timeInfosData);
        setFeaturable(timeInfosData);
    }

}
