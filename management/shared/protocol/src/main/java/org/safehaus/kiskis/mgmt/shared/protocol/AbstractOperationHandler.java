package org.safehaus.kiskis.mgmt.shared.protocol;

import java.util.UUID;

/**
 * Created by dilshat on 5/6/14.
 */
public abstract class AbstractOperationHandler<T> implements Runnable {

    protected final T manager;
    protected final String clusterName;

    public AbstractOperationHandler(T manager, String clusterName) {
        this.manager = manager;
        this.clusterName = clusterName;
    }

    public String getClusterName() {
        return clusterName;
    }

    public abstract UUID getTrackerId();

}
