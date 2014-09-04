package org.safehaus.subutai.plugin.storm.impl.handler;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.tracker.ProductOperationView;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.plugin.storm.impl.StormImpl;

class Helper {

    final StormImpl manager;

    public Helper(StormImpl manager) {
        this.manager = manager;
    }

    Agent createContainer() throws LxcCreateException {
        Set<Agent> s = createContainers(1);
        return s.size() > 0 ? s.iterator().next() : null;
    }

    Set<Agent> createContainers(int count) throws LxcCreateException {
        Map<Agent, Set<Agent>> lxcs = manager.getLxcManager().createLxcs(count);
        Set<Agent> res = new HashSet<>();
        for(Set<Agent> s : lxcs.values()) res.addAll(s);
        return res;
    }

    boolean installZookeeper(Agent agent) {
//        UUID id = manager.getZookeeperManager().install(agent.getHostname());
//        String src = org.safehaus.subutai.api.zookeeper.Config.PRODUCT_KEY;
//        return watchOperation(src, id, 2, TimeUnit.MINUTES);

        // TODO:
        return false;
    }

    boolean startZookeeper(Agent agent) {
//        UUID id = manager.getZookeeperManager().start(agent.getHostname());
//        String src = org.safehaus.subutai.api.zookeeper.Config.PRODUCT_KEY;
//        return watchOperation(src, id, 1, TimeUnit.MINUTES);

        // TODO:
        return false;
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
