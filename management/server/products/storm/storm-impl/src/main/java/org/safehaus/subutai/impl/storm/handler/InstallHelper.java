package org.safehaus.subutai.impl.storm.handler;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.safehaus.subutai.impl.storm.StormImpl;
import org.safehaus.subutai.shared.operation.ProductOperationState;
import org.safehaus.subutai.shared.operation.ProductOperationView;
import org.safehaus.subutai.shared.protocol.Agent;

class InstallHelper {

    final StormImpl manager;

    public InstallHelper(StormImpl manager) {
        this.manager = manager;
    }

    boolean installZookeeper(Agent agent) {
        UUID id = manager.getZookeeperManager().install(agent.getHostname());
        CountDownLatch latch = new CountDownLatch(1);
        new Thread(new Waiter(latch, id)).start();
        try {
            latch.await(2, TimeUnit.MINUTES);
        } catch(InterruptedException ex) {
            return false;
        }
        return true;
    }

    class Waiter implements Runnable {

        final CountDownLatch latch;
        final UUID trackerId;

        public Waiter(CountDownLatch latch, UUID trackerId) {
            this.latch = latch;
            this.trackerId = trackerId;
        }

        @Override
        public void run() {
            String zk = org.safehaus.subutai.api.zookeeper.Config.PRODUCT_KEY;
            ProductOperationView p = null;
            while(p == null || p.getState() == ProductOperationState.RUNNING) {
                p = manager.getTracker().getProductOperation(zk, trackerId);
            }
            latch.countDown();
        }

    }
}
