package org.safehaus.kiskis.mgmt.server.ui.modules.monitor.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Poll extends Thread {

    private final static Logger LOG = LoggerFactory.getLogger(Poll.class);

    private static final int PAUSE = 5000;

    private boolean interrupted;

    private Chart chart;

    Poll(Chart chart) {
        this.chart = chart;
    }

    public void run() {
        // We don't want this thread running eternally if the thread isn't stopped explicitly.
        for (int i = 0; i < 200; i++) {
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
