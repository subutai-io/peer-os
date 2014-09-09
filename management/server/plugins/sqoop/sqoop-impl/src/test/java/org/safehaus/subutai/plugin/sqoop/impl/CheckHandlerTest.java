package org.safehaus.subutai.plugin.sqoop.impl;

import org.junit.Test;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.sqoop.impl.handler.CheckHandler;
import org.safehaus.subutai.plugin.sqoop.impl.mock.SqoopImplMock;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;

public class CheckHandlerTest {
    @Test
    public void testWithoutNodes(){
        ProductOperation po = SqoopImplMock.getProductOperationMock();
        SqoopImplMock sqoopImplMock = new SqoopImplMock();
        CheckHandler checkHandler = new CheckHandler( sqoopImplMock.getSqoopImplMock(), "test", po );
        checkHandler.run();
        verify( po ).addLogFailed( "Sqoop installation not found:" + anyString() );
    }
}
