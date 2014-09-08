package org.safehaus.subutai.plugin.accumulo.impl.handler.mock;

import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.tracker.ProductOperationView;
import org.safehaus.subutai.core.tracker.api.Tracker;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TrackerMock implements Tracker {
    @Override
    public ProductOperationView getProductOperation(String source, UUID operationTrackId) {
        return null;
    }


    @Override
    public ProductOperation createProductOperation(String source, String description) {
//        return new ProductOperationMock();
        ProductOperation mock = mock(ProductOperation.class);
        when( mock.getId() ).thenReturn( null );
        when( mock.getDescription() ).thenReturn( null );
        when( mock.getState() ).thenReturn( ProductOperationState.RUNNING );
        return mock;
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