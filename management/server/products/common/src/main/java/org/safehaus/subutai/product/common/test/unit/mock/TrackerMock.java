package org.safehaus.subutai.product.common.test.unit.mock;


import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.tracker.ProductOperationView;

import java.util.Date;
import java.util.List;
import java.util.UUID;


public class TrackerMock implements Tracker {
	@Override
	public ProductOperationView getProductOperation(String source, UUID operationTrackId) {
		return null;
	}


	@Override
	public ProductOperation createProductOperation(String source, String description) {
		return new ProductOperationMock();
	}


	@Override
	public List<ProductOperationView> getProductOperations(String source, Date fromDate, Date toDate, int limit) {
		return null;
	}


	@Override
	public List<String> getProductOperationSources() {
		return null;
	}


	@Override
	public void printOperationLog(String source, UUID operationTrackId, long maxOperationDurationMs) {

	}
}
