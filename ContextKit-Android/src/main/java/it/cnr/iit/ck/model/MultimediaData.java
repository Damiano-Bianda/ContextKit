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

import it.cnr.iit.R;
import it.cnr.iit.ck.commons.Utils;
import it.cnr.iit.ck.logs.FileLogger;


public class MultimediaData implements Loggable, Featurable {

    private String name;
    private final MultimediaType type;

    public enum MultimediaType {IMAGE, VIDEO, DEFAULT}

    public MultimediaData(String fileName, MultimediaType type) {
        this.name = fileName;
        this.type = type;
    }

    /**
     * Creates a default MultimediaData type
     */
    public MultimediaData() {
        this.name = "";
        this.type = MultimediaType.DEFAULT;
    }

    @Override
    public String getRowToLog() {
        return Utils.formatStringForCSV(name) + FileLogger.SEP + Utils.formatStringForCSV(type.toString());
    }

    @Override
    public List<Double> getFeatures(Context context) {
        List<Double> features = new ArrayList<>();
        switch (type){
            case IMAGE:
                features.add(1d);
                features.add(0d);
                break;
            case VIDEO:
                features.add(0d);
                features.add(1d);
                break;
            case DEFAULT:
                features.add(0d);
                features.add(0d);
                break;
        }
        return features;
    }

}
