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

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.cnr.iit.R;
import it.cnr.iit.ck.logs.FileLogger;

public class OpenWeatherData implements Loggable, Featurable {

    private List<WeatherField> weather;
    private MainInfo main;
    private WindField wind;
    private CloudsField clouds;
    private RainField rain;
    private SnowField snow;
    private SysField sys;

    @Override
    public String getRowToLog() {

        return  (weather != null ? String.valueOf(weather.get(0).id) : 0) + FileLogger.SEP +
                (main != null ? String.valueOf(main.temp) : 0) + FileLogger.SEP +
                (main != null ? String.valueOf(main.temp_min) : 0) + FileLogger.SEP +
                (main != null ? String.valueOf(main.temp_max) : 0) + FileLogger.SEP +
                (main != null ? String.valueOf(main.humidity) : 0) + FileLogger.SEP +
                (main != null ? String.valueOf(main.pressure) : 0) + FileLogger.SEP +
                (wind != null ? String.valueOf(wind.speed) : 0) + FileLogger.SEP +
                (wind != null ? String.valueOf(wind.deg) : 0) + FileLogger.SEP +
                (clouds != null ? String.valueOf(clouds.all) : 0) + FileLogger.SEP +
                (rain != null ? String.valueOf(rain.last3hours) : 0) + FileLogger.SEP +
                (snow != null ? String.valueOf(snow.last3hours) : 0) + FileLogger.SEP +
                (sys != null ? String.valueOf(sys.sunrise) : 0) + FileLogger.SEP +
                (sys != null ? String.valueOf(sys.sunset) : 0);
    }

    @Override
    public List<Double> getFeatures(Context context) {
        List<Double> features = getOneHotVector(context);
        Collections.addAll(features,
                main != null ? (double) main.temp : 0,
                main != null ? (double) main.temp_min : 0,
                main != null ? (double) main.temp_max : 0,
                main != null ? (double) main.humidity : 0,
                main != null ? (double) main.pressure : 0,
                wind != null ? (double) wind.speed : 0,
                wind != null ? (double) wind.deg : 0,
                clouds != null ? (double) clouds.all : 0);
        return features;
    }

    /**
     * build the one hot encoding feature vector for weather id class
     * @param context
     * @return A list containing the one hot vector form of categorical attribute weather id
     */
    private List<Double> getOneHotVector(Context context) {
        int[] categories = context.getResources().getIntArray(R.array.weather_conditions_codes);
        int id = weather != null ? weather.get(0).id : 0;
        List<Double> oneHotVector = new ArrayList<>();
        for(int category: categories){
            oneHotVector.add(id == category? 1.0d : 0.0d);
        }
        return oneHotVector;
    }


    private class WeatherField{
        // Weather condition codes: http://www.openweathermap.com/weather-conditions
        int id;
    }

    private class MainInfo{ float temp, temp_min, temp_max, humidity, pressure; }

    private class WindField{ float speed, deg; }

    private class CloudsField{ float all; }

    private class RainField{ @SerializedName("3h") float last3hours; }

    private class SnowField{ @SerializedName("3h") float last3hours; }

    private class SysField{ long sunrise, sunset; }
}
