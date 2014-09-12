package org.safehaus.subutai.plugin.cassandra.ui.manager;

import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.protocol.CompleteEvent;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.tracker.ProductOperationView;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;
import org.safehaus.subutai.plugin.cassandra.ui.CassandraUI;

import java.util.UUID;

public class StopTask implements Runnable {

    private final String clusterName, hostname;
    private final CompleteEvent completeEvent;

    public StopTask(String clusterName, String lxcHostname, CompleteEvent completeEvent) {
        this.clusterName = clusterName;
        this.hostname = lxcHostname;
        this.completeEvent = completeEvent;
    }

    @Override
    public void run() {

        UUID trackID = CassandraUI.getCassandraManager().stopService( clusterName, hostname );

        long start = System.currentTimeMillis();
        NodeState state = NodeState.UNKNOWN;

        while(!Thread.interrupted()) {
            ProductOperationView po = CassandraUI.getTracker().getProductOperation( CassandraClusterConfig.PRODUCT_KEY, trackID);
            if(po != null)
                if(po.getState() != ProductOperationState.RUNNING) {
                    if(po.getState() == ProductOperationState.SUCCEEDED)
                        state = NodeState.STOPPED;
                    break;
                }
            try {
                Thread.sleep(1000);
            } catch(InterruptedException ex) {
                break;
            }
            if(System.currentTimeMillis() - start > 30 * 1000)
                break;
        }

        completeEvent.onComplete(state);
    }

}
