package org.safehaus.subutai.plugin.sqoop.impl;

import org.junit.Test;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.sqoop.impl.handler.DestroyNodeHandler;
import org.safehaus.subutai.plugin.sqoop.impl.mock.SqoopImplMock;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;

public class DestroyNodeHandlerTest {

    @Test
    public void testHadoopConfIsNotSpecified(){
        ProductOperation po = SqoopImplMock.getProductOperationMock();
        SqoopImplMock sqoopImplMock = new SqoopImplMock();
        DestroyNodeHandler destroyNodeHandler = new DestroyNodeHandler( sqoopImplMock.getSqoopImplMock(), "test", po );

        destroyNodeHandler.run();
        verify( po ).addLogFailed( "Sqoop installation not found:" + anyString() );
    }
}
