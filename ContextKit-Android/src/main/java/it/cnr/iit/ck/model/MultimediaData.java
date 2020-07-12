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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import it.cnr.iit.R;
import it.cnr.iit.ck.commons.Utils;
import it.cnr.iit.ck.logs.FileLogger;


public class MultimediaData implements Loggable, Featurable {

    private final String name;
    private final boolean isImage;
    private final boolean isVideo;

    public MultimediaData(String fileName, boolean isImage, boolean isVideo) {
        this.name = fileName;
        this.isImage = isImage;
        this.isVideo = isVideo;
    }

    @Override
    public String getRowToLog() {
        return Utils.formatStringForCSV(name) + FileLogger.SEP + isImage + "IMAGE" + isVideo + "VIDEO";
    }

    @Override
    public List<Double> getFeatures(Context context) {
        List<Double> features = new ArrayList<>();
        features.add(isImage? 1d : 0d);
        features.add(isVideo? 1d : 0d);
        return features;
    }

    public boolean isImage() {
        return isImage;
    }

    public boolean isVideo() {
        return isVideo;
    }

}
