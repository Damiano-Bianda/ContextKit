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

package it.cnr.iit.ckdemo;

import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import it.cnr.iit.ck.CK;
import it.cnr.iit.ck.CKScheduler;

/**
 * @author Mattia Campana (m.campana@iit.cnr.it)
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (CK.isRunning(getApplicationContext())){
            ((Button)findViewById(R.id.button2)).setText("STOP READING");
        } else {
            ((Button)findViewById(R.id.button2)).setText("START NEW READING");
        }
    }

    private void startASK(){
        String configuration = readTextFile(getResources().openRawResource(R.raw.configuration));
        CK.start(getApplicationContext(), configuration, false);
        /*long aMinute = 60000;
        CK.startPeriodically(getApplicationContext(),  configuration,120 * aMinute, 3000 * aMinute, false);
        CK.startPeriodically(getApplicationContext(),  configuration,10000, 20000, false);*/
    }

    private void stopASK(){
        CK.stop(getApplicationContext());
        //CK.stopPeriodically(getApplicationContext());
    }

    public String readTextFile(InputStream inputStream) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream.toString();
    }



    public void onControlClicked(View view){
        if(!CK.isRunning(getApplicationContext())){
            startASK();
            ((Button)view).setText("STOP READING");
        }else{
            stopASK();
            ((Button)view).setText("START NEW READING");
        }
    }
}
