package org.safehaus.subutai.plugin.sqoop.impl;


import org.junit.Test;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.plugin.sqoop.api.SetupType;
import org.safehaus.subutai.plugin.sqoop.api.SqoopConfig;
import org.safehaus.subutai.plugin.sqoop.impl.handler.InstallHandler;
import org.safehaus.subutai.plugin.sqoop.impl.mock.SqoopImplMock;

import static org.mockito.Mockito.verify;


public class InstallHandlerTest
{

    @Test
    public void testWithoutNodes()
    {
        TrackerOperation po = SqoopImplMock.getProductOperationMock();
        SqoopImplMock sqoopImplMock = new SqoopImplMock();
        InstallHandler installHandler = new InstallHandler( sqoopImplMock.getSqoopImplMock(), "test", po );
        SqoopConfig config = new SqoopConfig();
        config.setSetupType( SetupType.WITH_HADOOP );
        installHandler.setConfig( config );

        installHandler.run();
        verify( po ).addLogFailed( "No Hadoop configuration specified" );
    }
}
