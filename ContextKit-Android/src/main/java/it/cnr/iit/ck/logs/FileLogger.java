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

package it.cnr.iit.ck.logs;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import it.cnr.iit.ck.commons.Utils;
import it.cnr.iit.ck.model.Loggable;
import it.cnr.iit.ck.model.MultiLoggable;
import it.cnr.iit.ck.probes.BaseProbe;

public class FileLogger extends HandlerThread {

    //private static final String DEFAULT_BASE_DIR = "AndroidSensingKit";
    public static final String SEP = ",";
    private static FileLogger instance;

    private String basePath;
    private Handler handler;

    public void init(String folderName){
        this.basePath = Environment.getExternalStorageDirectory() + File.separator + folderName;
        File dir = new File(this.basePath);
        if(!dir.exists()) dir.mkdir();
        this.start();
        handler = new Handler(getLooper());
    }

    private FileLogger() {
        super("FileLogger Handler Thread");
    }

    public static FileLogger getInstance() {
        if (instance == null) {
            instance = new FileLogger();
        }
        return instance;
    }

    public void store(final String fileName, MultiLoggable data, final boolean withTimeStamp) {
        StringBuilder sb = new StringBuilder();
        long time = getTime();
        for (String printable : data.getRowsToLog()) {
            if (withTimeStamp) {
                sb.append(time).append(SEP);
            }
            sb.append(printable).append("\n");
        }
        String toWrite = sb.toString();
        handler.post(new WriterRunnable(toWrite, basePath + File.separator + fileName));
    }

    private long getTime() {
        return Calendar.getInstance().getTime().getTime();
    }

    public void store(final String fileName, final Loggable data, final boolean withTimeStamp) {
        String toWrite = withTimeStamp ?
                getTime() + SEP + data.getRowToLog()  + "\n" :
                data.getRowToLog() + "\n";
        handler.post(new WriterRunnable(toWrite, basePath + File.separator + fileName));
    }

    public void store(String fileName, String toWrite, final boolean withTimeStamp){
        toWrite = withTimeStamp ?
                getTime() + SEP + toWrite + "\n" :
                toWrite + "\n";
        handler.post(new WriterRunnable(toWrite, basePath + File.separator + fileName));
    }

    public boolean logFileIsEmptyOrDoesntExists(String fileName){
        return new File(basePath + File.separator + fileName).length() == 0;
    }

    public String getBasePath() {
        return basePath;
    }

    public void processQueueAndStop() throws InterruptedException {
        this.quitSafely();
        this.join();
        instance = null;
    }

    private static class WriterRunnable implements Runnable {

        private final String toWrite;
        private final String filePath;

        WriterRunnable(final String toWrite, final String filePath){
            this.toWrite = toWrite;
            this.filePath = filePath;
        }

        @Override
        public void run() {
            File file = checkForExistingOrCreateFile(filePath);

            FileWriter fw = null;
            try {
                fw= new FileWriter(file, true);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            try { fw.write(toWrite); } catch (IOException e) { e.printStackTrace(); }
            try { fw.close(); } catch (IOException e) { e.printStackTrace(); }
        }

        File checkForExistingOrCreateFile(String filePath) {
            File file = new File(filePath);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                }
                catch (IOException e) { e.printStackTrace(); }
            }
            return file;
        }
    }
}
