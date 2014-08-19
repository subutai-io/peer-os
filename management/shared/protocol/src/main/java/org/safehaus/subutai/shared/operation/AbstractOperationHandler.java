package org.safehaus.subutai.shared.operation;


import java.util.UUID;

import org.safehaus.subutai.shared.protocol.ApiBase;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


public abstract class AbstractOperationHandler<T extends ApiBase> implements Runnable {
    protected final T manager;
    protected final String clusterName;
    protected ProductOperation productOperation;


    public AbstractOperationHandler( T manager, String clusterName ) {
        Preconditions.checkNotNull( manager, "Manager is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        this.manager = manager;
        this.clusterName = clusterName;
    }


    public UUID getTrackerId() {
        return productOperation.getId();
    }


    public String getClusterName() {
        return clusterName;
    }


    public ProductOperation getProductOperation() {
        return productOperation;
    }
}
