package org.safehaus.subutai.plugin.sqoop.impl;

import org.junit.Test;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.sqoop.api.SqoopConfig;
import org.safehaus.subutai.plugin.sqoop.impl.handler.CheckHandler;
import org.safehaus.subutai.plugin.sqoop.impl.mock.SqoopImplMock;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;

public class CheckHandlerTest {
    @Test
    public void testWithoutConfiguration(){
        ProductOperation po = SqoopImplMock.getProductOperationMock();
        SqoopImplMock sqoopImplMock = new SqoopImplMock();
        CheckHandler checkHandler = new CheckHandler( sqoopImplMock.getSqoopImplMock(), "test", po );
        checkHandler.run();
        verify( po ).addLogFailed( "Sqoop installation not found:" + anyString() );
    }

    @Test
    public void testNodeNotConnected(){
        ProductOperation po = SqoopImplMock.getProductOperationMock();
        SqoopImplMock sqoopImplMock = new SqoopImplMock();
        SqoopConfig config = new SqoopConfig();
        config.setHadoopClusterName( "test" );
        sqoopImplMock.setSqoopConfig( config );
        CheckHandler checkHandler = new CheckHandler( sqoopImplMock.getSqoopImplConfiguredMock(), "test", po );

        checkHandler.run();
        verify( po ).addLogFailed( "Node is not connected" );
    }
}
