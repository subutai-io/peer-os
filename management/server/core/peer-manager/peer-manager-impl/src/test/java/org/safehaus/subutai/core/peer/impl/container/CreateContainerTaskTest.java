package org.safehaus.subutai.core.peer.impl.container;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.command.CommandUtil;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.peer.api.ResourceHost;


@RunWith( MockitoJUnitRunner.class )
public class CreateContainerTaskTest
{
    private static final String HOSTNAME = "hostname";
    private static final String IP = "127.0.0.1";
    private static final int VLAN = 100;
    private static final String GATEWAY = "192.168.1.1";
    private static final int TIMEOUT = 30;
    @Mock
    ResourceHost resourceHost;
    @Mock
    Template template;
    @Mock
    CommandUtil commandUtil;

    CreateContainerTask task;


    @Before
    public void setUp() throws Exception
    {
        task = new CreateContainerTask( resourceHost, template, HOSTNAME, IP, VLAN, GATEWAY, TIMEOUT );
    }


    @Test
    public void testPrepareTemplate() throws Exception
    {


    }
}
