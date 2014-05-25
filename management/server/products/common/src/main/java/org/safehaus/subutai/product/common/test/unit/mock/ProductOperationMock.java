package org.safehaus.subutai.product.common.test.unit.mock;


import java.util.Date;
import java.util.UUID;

import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.operation.ProductOperationState;


public class ProductOperationMock implements ProductOperation {
    private final StringBuilder log = new StringBuilder();

    private ProductOperationState state = ProductOperationState.RUNNING;


    @Override
    public String getLog() {
        return log.toString();
    }


    @Override
    public void addLog( String logString ) {
        log.append( logString );
    }


    @Override
    public void addLogDone( String logString ) {
        addLog( logString );
        state = ProductOperationState.SUCCEEDED;
    }


    @Override
    public void addLogFailed( String logString ) {
        addLog( logString );
        state = ProductOperationState.FAILED;
    }


    @Override
    public ProductOperationState getState() {
        return state;
    }


    @Override
    public String getDescription() {
        return null;
    }


    @Override
    public UUID getId() {
        return null;
    }


    @Override
    public Date createDate() {
        return null;
    }
}
