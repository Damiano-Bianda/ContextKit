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

package it.cnr.iit.ck.model;

import android.content.Context;
import android.media.AudioManager;

import java.util.Collections;
import java.util.List;

import it.cnr.iit.R;
import it.cnr.iit.ck.logs.FileLogger;

import static it.cnr.iit.ck.features.FeatureUtils.binarize;
import static it.cnr.iit.ck.features.FeatureUtils.oneHotVector;

public class AudioInfo implements Loggable, Featurable{

    private int ringerMode;
    private float alarmVolume, musicVolume, notificationVolume, ringVolume;
    private boolean bluetoothScoOn, microphoneMute, musicActive, speakerOn, headsetOn;

    public AudioInfo(int ringerMode, float alarmVolume, float musicVolume, float notificationVolume,
                     float ringVolume, boolean bluetoothScoOn, boolean microphoneMute,
                     boolean musicActive, boolean speakerOn, boolean headsetOn){

        this.ringerMode = ringerMode;
        this.alarmVolume = alarmVolume;
        this.musicVolume = musicVolume;
        this.notificationVolume = notificationVolume;
        this.ringVolume = ringVolume;
        this.bluetoothScoOn = bluetoothScoOn;
        this.microphoneMute = microphoneMute;
        this.musicActive = musicActive;
        this.speakerOn = speakerOn;
        this.headsetOn = headsetOn;
    }

    @Override
    public String getRowToLog() {
        String s = ringerMode + FileLogger.SEP + alarmVolume + FileLogger.SEP + musicVolume +
                FileLogger.SEP + notificationVolume + FileLogger.SEP + ringVolume + FileLogger.SEP +
                bluetoothScoOn + FileLogger.SEP + microphoneMute + FileLogger.SEP +
                musicActive + FileLogger.SEP + speakerOn + FileLogger.SEP + headsetOn;
        return s;
    }

    @Override
    public List<Double> getFeatures(Context context) {
        List<Double> features = oneHotVector(ringerMode, AudioManager.RINGER_MODE_SILENT, AudioManager.RINGER_MODE_VIBRATE, AudioManager.RINGER_MODE_NORMAL);
        Collections.addAll(features, (double) alarmVolume, (double) musicVolume, (double) notificationVolume, (double) ringVolume,
                binarize(bluetoothScoOn),
                binarize(microphoneMute),
                binarize(musicActive),
                binarize(speakerOn),
                binarize(headsetOn));
        return features;
    }

}
