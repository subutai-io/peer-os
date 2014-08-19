package org.safehaus.subutai.shared.operation;


import org.safehaus.subutai.shared.protocol.ApiBase;

import java.util.UUID;


public abstract class AbstractOperationHandler<T extends ApiBase> implements Runnable {
	protected final T manager;
	protected final String clusterName;
	protected ProductOperation productOperation;


	public AbstractOperationHandler(T manager, String clusterName) {
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
