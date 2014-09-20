package org.safehaus.subutai.plugin.sqoop.impl;


import org.junit.Test;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.sqoop.api.SqoopConfig;
import org.safehaus.subutai.plugin.sqoop.impl.handler.DestroyNodeHandler;
import org.safehaus.subutai.plugin.sqoop.impl.mock.SqoopImplMock;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;


public class DestroyNodeHandlerTest
{

    @Test
    public void testHadoopConfIsNotSpecified()
    {
        ProductOperation po = SqoopImplMock.getProductOperationMock();
        SqoopImplMock sqoopImplMock = new SqoopImplMock();
        DestroyNodeHandler destroyNodeHandler = new DestroyNodeHandler( sqoopImplMock.getSqoopImplMock(), "test", po );

        destroyNodeHandler.run();
        verify( po ).addLogFailed( "Sqoop installation not found:" + anyString() );
    }


    @Test
    public void testNodeNotConnected()
    {
        ProductOperation po = SqoopImplMock.getProductOperationMock();
        SqoopImplMock sqoopImplMock = new SqoopImplMock();
        SqoopConfig config = new SqoopConfig();
        config.setHadoopClusterName( "test" );
        sqoopImplMock.setSqoopConfig( config );
        DestroyNodeHandler checkHandler =
                new DestroyNodeHandler( sqoopImplMock.getSqoopImplConfiguredMock(), "test", po );

        checkHandler.run();
        verify( po ).addLogFailed( "Node is not connected" );
    }


    @Test
    public void testNodeDoesNotBelong()
    {
        ProductOperation po = SqoopImplMock.getProductOperationMock();
        SqoopImplMock sqoopImplMock = new SqoopImplMock();
        SqoopConfig config = new SqoopConfig();
        config.setHadoopClusterName( "test" );
        sqoopImplMock.setSqoopConfig( config );
        DestroyNodeHandler checkHandler =
                new DestroyNodeHandler( sqoopImplMock.getSqoopImplConfigurationNotBelongToSqoopNodes(), "test", po );

        checkHandler.run();
        verify( po ).addLogFailed( "Node does not belong to Sqoop installation group" );
    }
}
