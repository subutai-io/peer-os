package org.safehaus.subutai.impl.storm.handler;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.safehaus.subutai.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.impl.storm.StormImpl;
import org.safehaus.subutai.shared.operation.ProductOperationState;
import org.safehaus.subutai.shared.operation.ProductOperationView;
import org.safehaus.subutai.shared.protocol.Agent;

class InstallHelper {

    final StormImpl manager;

    public InstallHelper(StormImpl manager) {
        this.manager = manager;
    }

    Set<Agent> createSupervisorContainers(int count) throws LxcCreateException {
        Map<Agent, Set<Agent>> lxcs = manager.getLxcManager().createLxcs(count);
        Set<Agent> res = new HashSet<>();
        for(Set<Agent> s : lxcs.values()) res.addAll(s);
        return res;
    }

    Agent createNimbusContainer() throws LxcCreateException {
        Map<Agent, Set<Agent>> map = manager.getLxcManager().createLxcs(1);
        Collection<Set<Agent>> coll = map.values();
        if(coll.size() > 0) {
            Set<Agent> set = coll.iterator().next();
            if(set.size() > 0) return set.iterator().next();
        }
        return null;
    }

    boolean installZookeeper(Agent agent) {
        UUID id = manager.getZookeeperManager().install(agent.getHostname());
        CountDownLatch latch = new CountDownLatch(1);
        new Thread(new OperationStatusChecker(latch, id)).start();
        try {
            latch.await(2, TimeUnit.MINUTES);
        } catch(InterruptedException ex) {
            return false;
        }
        return true;
    }

    class OperationStatusChecker implements Runnable {

        final CountDownLatch latch;
        final UUID trackerId;

        public OperationStatusChecker(CountDownLatch latch, UUID trackerId) {
            this.latch = latch;
            this.trackerId = trackerId;
        }

        @Override
        public void run() {
            String zk = org.safehaus.subutai.api.zookeeper.Config.PRODUCT_KEY;
            ProductOperationView p = null;
            while(p == null || p.getState() == ProductOperationState.RUNNING) {
                p = manager.getTracker().getProductOperation(zk, trackerId);
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
