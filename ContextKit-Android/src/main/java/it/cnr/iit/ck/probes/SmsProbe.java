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
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import it.cnr.iit.R;
import it.cnr.iit.ck.controllers.SmsController;
import it.cnr.iit.ck.model.LoggableElements;
import it.cnr.iit.ck.model.Sms;

/**
 * This probe monitors both the received and sent sms. During the first run, it read all the
 * received/sent sms in the history of the local device.
 *
 * Required permissions:
 *
 *      - "android.permission.READ_SMS"
 *      - "android.permission.READ_CONTACTS"
 *      - "android.permission.RECEIVE_SMS"
 *
 */
@SuppressWarnings("unused")
// TODO guardare se e tutto apposto
public class SmsProbe extends OnEventProbe {

    private OutGoingObserver outGoingObserver;
    private Long lastReceivedSmsID;
    private Long lastSentSmsID;

    public SmsProbe(){}

    @Override
    public void init() {

        outGoingObserver = new OutGoingObserver(getHandler());

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);

        ContentResolver contentResolver = getContext().getContentResolver();
        contentResolver.registerContentObserver(Telephony.Sms.CONTENT_URI,
                true, outGoingObserver);

        Sms lastReceivedSms = SmsController.getLastReceivedSms(getContext());
        lastReceivedSmsID = lastReceivedSms == null ? Long.MIN_VALUE : lastReceivedSms.getId();

        Sms lastSentSms = SmsController.getLastSentSms(getContext());
        lastSentSmsID = lastSentSms == null ? Long.MIN_VALUE : lastSentSms.getId();

    }

    @Override
    public void onFirstRun() {
        fetchAllSms();
    }

    private void fetchAllSms() {
        logOnFile(true, new LoggableElements(SmsController.getAllSms(getContext())));
    }

    @Override
    void onStop() {

        if(outGoingObserver != null) {
            ContentResolver contentResolver = getContext().getContentResolver();
            contentResolver.unregisterContentObserver(outGoingObserver);
        }
        outGoingObserver = null;
    }

    @Override
    public boolean featuresData() {
        return false;
    }

    @Override
    public String[] getLogFileHeaders() {
        return getContext().getResources().getStringArray(R.array.sms_log_file_headers);
    }

    private void onSmSEvent(Sms sms){
        logOnFile(true, sms);
    }

    class OutGoingObserver extends ContentObserver {

        OutGoingObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {

            Sms outgoingSms = SmsController.getLastSentSms(getContext());
            if(outgoingSms != null && outgoingSms.getId() != lastSentSmsID){
                onSmSEvent(outgoingSms);
                lastSentSmsID = outgoingSms.getId();
            }

            Sms receivedSms = SmsController.getLastReceivedSms(getContext());
            if(receivedSms != null && receivedSms.getId() != lastReceivedSmsID){
                onSmSEvent(receivedSms);
                lastReceivedSmsID = receivedSms.getId();
            }
        }
    }

}
