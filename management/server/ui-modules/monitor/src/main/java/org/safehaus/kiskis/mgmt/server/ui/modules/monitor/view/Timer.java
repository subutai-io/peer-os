package org.safehaus.kiskis.mgmt.server.ui.modules.monitor.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Timer extends Thread {

    private final static Logger LOG = LoggerFactory.getLogger(Timer.class);

    private static final int PAUSE = 5000;

    private boolean interrupted;

    private Chart chart;

    Timer(Chart chart) {
        this.chart = chart;
    }

    public void run() {
        while (true) {
            pause();
            chart.push();

            if (interrupted) {
                break;
            }
        }
    }

    private static void pause() {
        try {
            sleep(PAUSE);
        } catch (InterruptedException e) {
            LOG.error("Timer interrupted: ", e);
        }
    }

    @Override
    public void interrupt() {
        super.interrupt();
        interrupted = true;
    }
}
