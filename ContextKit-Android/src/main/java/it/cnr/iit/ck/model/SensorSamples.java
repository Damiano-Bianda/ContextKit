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
import android.util.Log;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.cnr.iit.R;
import it.cnr.iit.ck.logs.FileLogger;

public class SensorSamples {

    public static final String[] STATISTIC_NAMES = {
            "min",
            "max",
            "mean",
            "quadratic_mean",
            "25_percentile",
            "50_percentile",
            "75_percentile",
            "variance",
            "population_variance",
            "sumsq",
            "standard_deviation"
    };

    public static final int MIN_ELEMENTS = 4;

    private int maxElements;

    private DescriptiveStatistics[] dimensions;
    private Float[] lastElements;

    /**
     * Create a new SensorSamples with constrained window
     * @param nDimensions: data dimensionality
     * @param maxElements: size of the window, infinite if maxELements less than 4
     */
    public SensorSamples(int nDimensions, Integer maxElements){
        this.maxElements = maxElements;
        dimensions = new DescriptiveStatistics[nDimensions];
        lastElements = new Float[nDimensions];
        for(int i=0; i<nDimensions; i++) {
            dimensions[i] = hasInfiniteWindow() ? new DescriptiveStatistics() :
                    new DescriptiveStatistics(maxElements);
        }
    }

    /**
     * Create a new SensorSamples with unconstrained window
     * @param nDimensions
     */
    public SensorSamples(int nDimensions){
        this(nDimensions, 0);
    }

    private boolean hasInfiniteWindow() {
        return this.maxElements < MIN_ELEMENTS;
    }

    /**
     * Add a value to the window, if it is full overwrite in FIFO mode.
     * last added values is cached and keeped also if window is cleared.
     * @param values
     */
    public void newSample(float[] values){
        for(int i=0; i<dimensions.length; i++) {
            dimensions[i].addValue(values[i]);
            lastElements[i] = values[i];
        }
    }

    /**
     * Generate stats about data window
     * @return an array containing stats, some elements can be NaN if window has less than 4 elements
     *      or all elements are all the same
     */
    public List<Double> getStatistics() {

        List<Double> stats = new ArrayList<>();

        for(DescriptiveStatistics dim : dimensions){
            stats.add(dim.getMin());
            stats.add(dim.getMax());
            stats.add(dim.getMean());
            stats.add(dim.getQuadraticMean());
            stats.add(dim.getPercentile(25));
            stats.add(dim.getPercentile(50));
            stats.add(dim.getPercentile(75));
            stats.add(dim.getVariance());
            stats.add(dim.getPopulationVariance());
            stats.add(dim.getSumsq());
            stats.add(dim.getStandardDeviation());
        }

        return stats;
    }

    /**
     * Empty the window
     */
    public void reset(){
        for(DescriptiveStatistics ds : dimensions) ds.clear();
    }

    /**
     * Pad, if possible, the samples window with the last element until this has max number of
     * elements specified during construction.
     * If current window has no elements but a previous data is cached this is used to pad the new
     * window.
     * @return true if object has been padded: there is at least one element in window or in cache
     * @throws CanNotPadAnInfiniteWindowException if window is infinite
     */
    public boolean padWindowWithLastElement() throws CanNotPadAnInfiniteWindowException {
        if (hasInfiniteWindow())
            throw new CanNotPadAnInfiniteWindowException();
        return padWindowWithLastSampleUntilNSamples(maxElements);
    }

    public class CanNotPadAnInfiniteWindowException extends Exception{
        public CanNotPadAnInfiniteWindowException(){
            super("Can not pad an infinite window in class SensorSamples");
        }
    }

    /**
     * Pad, if possible, the samples window with the last element until this has at least the number
     * of elements required to calculate all statistics (4).
     * If current window has no elements but a previous data is cached this is used to pad the new
     * window.
     * @return true if object has been padded: there is at least one element in window or in cache
     */
    public boolean padWindowWithLastElementUntilMinQuantityOfSamples() {
        return padWindowWithLastSampleUntilNSamples(MIN_ELEMENTS);
    }

    /**
     * Pad, if possible, a window with the last element until it has n elements.
     * If current window has no elements but a previous data is cached this is used to pad the new
     * window.
     * @param n: number of elements
     * @return true if object has been padded: there is at least one element in window or in cache
     */
    private boolean padWindowWithLastSampleUntilNSamples(int n) {

        for(int i = 0; i < dimensions.length; i++) {
            DescriptiveStatistics ds = dimensions[i];

            if (ds.getN() == 0) {
                if (lastElements[i] == null) return false;
                else ds.addValue(lastElements[i]);
            }

            double element = ds.getElement((int) ds.getN() - 1);
            while (ds.getN() < n) {
                ds.addValue(element);
            }
        }

        return true;
    }

    /**
     * number of elements in the window
     * @return number of elements in the window
     */
    public long length(){
        return dimensions[0].getN();
    }
}
