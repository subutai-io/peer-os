package org.safehaus.kiskis.mgmt.shared.operation;


import java.util.UUID;

import org.safehaus.kiskis.mgmt.shared.protocol.ApiBase;


public abstract class AbstractOperationHandler<T extends ApiBase> implements Runnable {
    protected final T manager;
    protected final String clusterName;
    protected ProductOperation productOperation;


    public AbstractOperationHandler( T manager, String clusterName ) {
        this.manager = manager;
        this.clusterName = clusterName;
    }


    public abstract UUID getTrackerId();


    public String getClusterName() {
        return clusterName;
    }


    public ProductOperation getProductOperation() {
        return productOperation;
    }
}
