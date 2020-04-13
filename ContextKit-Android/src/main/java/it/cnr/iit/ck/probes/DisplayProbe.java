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
import android.hardware.display.DisplayManager;
import android.util.Log;
import android.view.Display;

import it.cnr.iit.R;
import it.cnr.iit.ck.model.DisplayInfo;

/**
 * This probe monitors the display status, i.e., if it is on and its current rotation grade.
 *
 */
@SuppressWarnings("unused")
class DisplayProbe extends OnEventProbe {

    private DisplayManager displayManager;
    private DisplayManager.DisplayListener displayListener;

    @Override
    public void init() {

        displayManager = (DisplayManager) getContext().getSystemService(Context.DISPLAY_SERVICE);

        displayListener = new DisplayManager.DisplayListener() {
            @Override
            public void onDisplayAdded(int displayId) {
            }

            @Override
            public void onDisplayRemoved(int displayId) {
            }

            @Override
            public void onDisplayChanged(int displayId) {
                if (displayId == Display.DEFAULT_DISPLAY) {
                    fetchData();
                }
            }
        };
        displayManager.registerDisplayListener(displayListener, getHandler());

        fetchData();
    }

    @Override
    public void onFirstRun() {
    }

    @Override
    void onStop() {
        displayManager.unregisterDisplayListener(displayListener);
    }

    @Override
    public boolean featuresData() {
        return true;
    }

    @Override
    public String[] getFeaturesHeaders() {
        return getContext().getResources().getStringArray(R.array.display_feature_headers);
    }

    @Override
    public String[] getLogFileHeaders() {
        return getContext().getResources().getStringArray(R.array.display_log_file_headers);
    }

    private void fetchData(){
        DisplayInfo data = new DisplayInfo(displayManager.getDisplay(
                Display.DEFAULT_DISPLAY));
        logOnFile(true, data);
        post(data);
    }
}
