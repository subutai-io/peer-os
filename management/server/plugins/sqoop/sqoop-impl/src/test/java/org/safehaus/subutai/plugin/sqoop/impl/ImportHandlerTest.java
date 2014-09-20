package org.safehaus.subutai.plugin.sqoop.impl;


import org.junit.Test;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.sqoop.impl.handler.ImportHandler;
import org.safehaus.subutai.plugin.sqoop.impl.mock.SqoopImplMock;

import static org.mockito.Mockito.verify;


public class ImportHandlerTest
{

    @Test
    public void testWithNotConnectedNodes()
    {
        ProductOperation po = SqoopImplMock.getProductOperationMock();
        SqoopImplMock sqoopImplMock = new SqoopImplMock();
        AbstractOperationHandler operationHandler = new ImportHandler( sqoopImplMock.getSqoopImplMock(), "test", po );
        operationHandler.run();
        verify( po ).addLogFailed( "Node is not connected" );
    }
}
