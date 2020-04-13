package it.cnr.iit.ck.workers;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import it.cnr.iit.ck.probes.BaseProbe;
import it.cnr.iit.ck.probes.ContinuousProbe;
import it.cnr.iit.ck.probes.OnEventProbe;

public class Worker extends HandlerThread {

    private enum ProbeType {ON_EVENT, CONTINUOUS};
    private static final int START_MESSAGE = 1;
    private static final int EXEC_MESSAGE = 2;
    private static final int STOP_MESSAGE = 3;

    private final BaseProbe probe;
    private final ProbeType probeType;

    private boolean isFirstRun;

    private Handler handler;

    public Worker(OnEventProbe onEventProbe, final boolean isFirstRun) {
        super(onEventProbe.getClass().getSimpleName() + "Worker");
        this.isFirstRun = isFirstRun;
        this.probe = onEventProbe;
        this.probeType = ProbeType.ON_EVENT;
    }

    public Worker(ContinuousProbe continuousProbe, final boolean isFirstRun) {
        super(continuousProbe.getClass().getSimpleName() + "Worker");
        this.isFirstRun = isFirstRun;
        this.probe = continuousProbe;
        this.probeType = ProbeType.CONTINUOUS;
    }

    public void init(){
        this.start();
        this.handler = buildHandler();
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        // TODO controllare se lo esegue il nuovo thread
        probe.setHandler(buildHandler());
    }

    private Handler buildHandler() {
        switch (probeType){
            case ON_EVENT:
                return new OnEventHandler(getLooper());
            case CONTINUOUS:
                return new ContinuousHandler(getLooper());
        }
        throw new RuntimeException("Can not be here");
    }

    public void execute(){
        handler.sendEmptyMessage(START_MESSAGE);
    }

    public void terminate() throws InterruptedException {
        handler.sendEmptyMessage(STOP_MESSAGE);
        this.quitSafely();
        this.join();
    }

    private abstract class EventHandler extends Handler{

        public EventHandler(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case START_MESSAGE:
                    startBehaviour();
                    break;
                case EXEC_MESSAGE:
                    execBehaviour();
                    break;
                case STOP_MESSAGE:
                    stopBehaviour();
                    break;
            }
        }

        /**
         * Setup for a probe: init and call for an exec after initial user specified delay
         */
        public void startBehaviour() {
            probe.init();
            long delay = probe.getStartDelay() * 1000;
            if (delay > 0) {
                this.sendEmptyMessageDelayed(EXEC_MESSAGE, delay);
            } else {
                this.sendEmptyMessage(EXEC_MESSAGE);
            }
        }

        /**
         * Execution for a probe
         */
        public abstract void execBehaviour();

        /**
         * Stop for a probe
         */
        public void stopBehaviour() {
            this.removeMessages(START_MESSAGE);
            this.removeMessages(EXEC_MESSAGE);
            probe.stop();
        }

    }

    private class OnEventHandler extends EventHandler {

        public OnEventHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void execBehaviour() {
            if (isFirstRun) {
                probe.onFirstRun();
                isFirstRun = false;
            }
        }

    }

    private class ContinuousHandler extends EventHandler {

        public ContinuousHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void execBehaviour() {
            ContinuousProbe continuousProbe = (ContinuousProbe) probe;
            if (isFirstRun) {
                continuousProbe.onFirstRun();
                isFirstRun = false;
            }
            continuousProbe.exec();
            this.sendEmptyMessageDelayed(EXEC_MESSAGE, continuousProbe.getInterval() * 1000);
        }

    }

}
