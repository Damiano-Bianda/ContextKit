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

import android.app.usage.UsageStatsManager;

import java.util.List;

import it.cnr.iit.R;
import it.cnr.iit.ck.controllers.AppsUsageController;
import it.cnr.iit.ck.model.PackagesData;

/**
 * This probe monitors the usage statistics of the running applications using the
 * {@link UsageStatsManager}. For this reason, it requires that the minimum API level supported by
 * the app is >= 22.
 *
 * Parameters:
 *      - "lastNMinutes" : controls the size of the time used to identify the running applications.
 *      For example, with "lastNMinutes" : 5, this probe considers as "running" all the apps with
 *      last usage timestamp >= 5 minutes ago. The default value is 5 minutes.
 *
 * Requires:
 *      - "android.permission.PACKAGE_USAGE_STATS"
 */
@SuppressWarnings("all")
class RunningApplicationsProbe extends ContinuousProbe {

    private static final int DEFAULT_LAST_N_MINUTES = 5;
    private static final int PLAY_STORE_HTTP_REQUEST_MAX_TIMEOUT = 5000;

    private int lastNMinutes = DEFAULT_LAST_N_MINUTES;

    @Override
    public void init() {

    }

    @Override
    public void onFirstRun() {}

    @Override
    void onStop() {
    }

    @Override
    public boolean featuresData() {
        return true;
    }

    @Override
    public String[] getFeaturesHeaders() {
        return getContext().getResources().getStringArray(R.array.running_applications_categories_feature_headers);
    }

    @Override
    public String[] getLogFileHeaders() {
        return getContext().getResources().getStringArray(R.array.running_applications_log_file_headers);
    }

    @Override
    public void exec() {

        PackagesData packagesData = new PackagesData();
        List<String> recentApplications = AppsUsageController.getRecentApplications(getContext(), lastNMinutes);
        packagesData.setPackages(recentApplications);

        logOnFile(true, packagesData);
        setFeaturable(packagesData);

    }

}
