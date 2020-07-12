/*
 *   Copyright (c) 2017. Mattia Campana, mattia.campana@iit.cnr.it, Franca Delmastro, franca.delmastro@gmail.com
 *
 *   This file is part of ContextKit.
 *
 *   ContextKit (CK) is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   ContextKit (CK) is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with ContextKit (CK).  If not, see <http://www.gnu.org/licenses/>.
 */

package it.cnr.iit.ck.probes;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import it.cnr.iit.ck.CK;
import it.cnr.iit.ck.commons.Utils;
import it.cnr.iit.ck.features.FeatureMessage;
import it.cnr.iit.ck.features.FeaturesWorker;
import it.cnr.iit.ck.logs.FileLogger;
import it.cnr.iit.ck.model.Featurable;
import it.cnr.iit.ck.model.Loggable;
import it.cnr.iit.ck.model.MultiLoggable;

/**
 * This is the probes' base class.
 *
 * Parameters:
 *  - "logFile" : if present, defines the name of the logFeatures file.
 *  - "startDelay" : if present, delays the start of the probe
 *
 */
public abstract class BaseProbe {

    String logFile;
    private Context context;
    private int startDelay;
    private volatile Handler handler;
    private Featurable featurable;

    public void setLogFile(String logFile){ this.logFile = logFile; }

    public void setContext(Context context){this.context = context;}
    Context getContext(){
        return this.context;
    }

    public void setStartDelay(int startDelay){this.startDelay = startDelay;}
    public int getStartDelay(){return startDelay;}

    public void stop(){ this.onStop(); }

    public abstract void init();
    public abstract void onFirstRun();
    abstract void onStop();

    void logOnFile(boolean withTimeStamp, MultiLoggable data) {
        if(logFile != null) {
            if (FileLogger.getInstance().logFileIsEmptyOrDoesntExists(logFile))
                FileLogger.getInstance().store(logFile, TextUtils.join(FileLogger.SEP, getLogFileHeaders()), false);
            if (data.isEmpty()){
                if (CK.LOG_EMPTY_MULTI_LOGGABLE_DATA)
                    FileLogger.getInstance().store(logFile, Utils.formatStringForCSV("multiloggable_empty"), withTimeStamp);
            } else {
                FileLogger.getInstance().store(logFile, data, withTimeStamp);
            }
        }
    }

    void logOnFile(boolean withTimeStamp, Loggable data){
        if(logFile != null) {
            if (FileLogger.getInstance().logFileIsEmptyOrDoesntExists(logFile))
                FileLogger.getInstance().store(logFile, TextUtils.join(FileLogger.SEP, getLogFileHeaders()), false);
            FileLogger.getInstance().store(logFile, data, withTimeStamp);
        }
    }

    void logOnRedundantFile(boolean withTimeStamp, MultiLoggable data){
        String redundantLogFile = "redundant_" + logFile;
        if(redundantLogFile != null) {
            if (FileLogger.getInstance().logFileIsEmptyOrDoesntExists(redundantLogFile))
                FileLogger.getInstance().store(redundantLogFile, TextUtils.join(FileLogger.SEP, getLogFileHeaders()), false);
            FileLogger.getInstance().store(redundantLogFile, data, withTimeStamp);
        }
    }

    public abstract boolean featuresData();

    protected synchronized void setFeaturable(Featurable featurable){
        this.featurable = featurable;
    }

    public synchronized List<Double> getFeatures(Context context){
        if (featurable == null){
            return Collections.nCopies(getFeaturesHeaders().length, Double.NaN);
        } else {
            return featurable.getFeatures(context);
        }
    }

    public boolean isSupportedByDevice(){
        return true;
    }

    public String[] getFeaturesHeaders(){
        return new String[0];
    }
    public abstract String[] getLogFileHeaders();

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public Handler getHandler() {
        return handler;
    }
}
