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
import android.content.pm.ApplicationInfo;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import it.cnr.iit.R;
import it.cnr.iit.ck.commons.Utils;
import it.cnr.iit.ck.features.PlayStoreStorage;
import it.cnr.iit.ck.logs.FileLogger;

public class PackagesData implements Loggable, Featurable{

    private List<String> packages = new ArrayList<>();

    public PackagesData(List<ApplicationInfo> info){
        for (ApplicationInfo packageInfo : info) packages.add(packageInfo.packageName);
    }

    public PackagesData(){}

    @Override
    public String getRowToLog() {
        return Utils.formatStringForCSV(StringUtils.join(packages, FileLogger.SEP));
    }

    public void setPackages(List<String> packages) {
        this.packages = packages;
    }

    @Override
    public List<Double> getFeatures(Context context){
        LinkedHashMap<String, Double> activeCategoriesCounters = loadOrderedCategoryToCountMap(context);

        int total = 0;
        for(String activePackage: packages){
            String appCategory = PlayStoreStorage.readAppCategory(context, activePackage);
            if(appCategory != null && !appCategory.equals(PlayStoreStorage.UNKNOWN_APP_CATEGORY)) {
                Double counter = activeCategoriesCounters.get(appCategory);
                if (counter == null) {
                    Utils.logWarning(R.string
                            .no_app_category_in_this_version_of_CK_warning_message, context, appCategory);
                } else {
                    activeCategoriesCounters.put(appCategory, counter + 1);
                    total++;
                }
            }
        }

        List<Double> features = new ArrayList<>();
        for(Map.Entry<String, Double> entry: activeCategoriesCounters.entrySet()){
            features.add(entry.getValue() / (total == 0 ? 1 : total));
        }
        return features;
    }

    /**
     * Load an ordered map from resources that links category name (key) to a counter initially set to 0.
     * @return a LinkedHashMap that keeps the order of contained entries
     */
    private LinkedHashMap<String, Double> loadOrderedCategoryToCountMap(Context context) {
        LinkedHashMap<String, Double> activeCategoriesCounters = new LinkedHashMap<>();
        for(String category: context.getResources().getStringArray(R.array.play_store_categories)){
            activeCategoriesCounters.put(category, 0d);
        }
        return activeCategoriesCounters;
    }

}
