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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.List;

import it.cnr.iit.R;
import it.cnr.iit.ck.model.PackagesData;

/**
 * This probe monitors the installed applications. For each application, it reports the package
 * name.
 *
 */
@SuppressWarnings("unused")
class InstalledAppsProbe extends ContinuousProbe {

    @Override
    public void init() {}

    @Override
    public void onFirstRun() { }

    @Override
    void onStop() { }

    @Override
    public boolean featuresData() {
        return false;
    }

    @Override
    public String[] getLogFileHeaders() {
        return getContext().getResources().getStringArray(R.array.installed_apps_log_file_headers);
    }

    @Override
    public void exec() {
        fetchPackages();
    }

    private void fetchPackages(){
        final PackageManager pm = getContext().getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        PackagesData data = new PackagesData(packages);
        logOnFile(true, data);
    }

}
