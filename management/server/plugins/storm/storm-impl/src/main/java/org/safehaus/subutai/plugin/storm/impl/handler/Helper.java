package org.safehaus.subutai.plugin.storm.impl.handler;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.tracker.ProductOperationView;
import org.safehaus.subutai.plugin.storm.impl.StormImpl;

class Helper {

    final StormImpl manager;

    public Helper(StormImpl manager) {
        this.manager = manager;
    }

    boolean startZookeeper(String clusterName, String hostname) {
        UUID id = manager.getZookeeperManager().startNode(clusterName, hostname);
        String src = org.safehaus.subutai.api.zookeeper.Config.PRODUCT_KEY;
        return watchOperation(src, id, 1, TimeUnit.MINUTES);
    }

    private boolean watchOperation(String source, UUID id, long timeout, TimeUnit unit) {
        CountDownLatch latch = new CountDownLatch(1);
        new Thread(new OperationStatusChecker(latch, source, id)).start();
        try {
            return latch.await(timeout, unit);
        } catch(InterruptedException ex) {
            return false;
        }
    }

    class OperationStatusChecker implements Runnable {

        final CountDownLatch latch;
        final String source;
        final UUID trackerId;

        public OperationStatusChecker(CountDownLatch latch, String source, UUID trackerId) {
            this.latch = latch;
            this.source = source;
            this.trackerId = trackerId;
        }

        @Override
        public void run() {
            if(source != null && trackerId != null) {
                ProductOperationView p = null;
                while(p == null || p.getState() == ProductOperationState.RUNNING) {
                    p = manager.getTracker().getProductOperation(source, trackerId);
                    try {
                        Thread.sleep(200);
                    } catch(InterruptedException ex) {
                        break;
                    }
                }
                latch.countDown();
            }
        }

    }
}
