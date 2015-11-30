package io.subutai.core.localpeer.impl.container;


import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandUtil;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.peer.ResourceHostException;
import io.subutai.common.protocol.TemplateKurjun;
import io.subutai.common.settings.Common;
import io.subutai.core.hostregistry.api.HostRegistry;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class CreateContainerTaskTest
{
    private static final String HOSTNAME = "hostname";
    private static final String IP = "127.0.0.1";
    private static final String CIDR = "192.168.1.0/24";
    private static final int VLAN = 100;
    private static final String ENV_ID = "123";
    private static final String TEMPLATE_NAME = "master";
    private static final int TIMEOUT = 30;
    private static final String OUT = "TEMPLATE\n" + "--------\n" + "master";

    @Mock
    ResourceHost resourceHost;
    @Mock
    TemplateKurjun template;
    @Mock
    CommandUtil commandUtil;
    @Mock
    CommandResult commandResult;
    @Mock
    ContainerHost containerHost;

    CreateContainerTask task;
    @Mock
    private HostRegistry hostRegistry;


    @Before
    public void setUp() throws Exception
    {
        task = new CreateContainerTask( hostRegistry, resourceHost, template, HOSTNAME, CIDR, VLAN,
                TIMEOUT/*, ENV_ID*/ );
        task.commandUtil = commandUtil;
        when( resourceHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );
        when( commandResult.hasSucceeded() ).thenReturn( true );
        when( commandResult.getStdOut() ).thenReturn( OUT );
        when( template.getName() ).thenReturn( TEMPLATE_NAME );
        when( resourceHost.getContainerHostByName( HOSTNAME ) ).thenReturn( containerHost );
        when( containerHost.getIpByInterfaceName( Common.DEFAULT_CONTAINER_INTERFACE ) ).thenReturn( IP );
    }


    @Test( expected = ResourceHostException.class )
    public void testPrepareTemplate() throws Exception
    {
        when( commandResult.getStdOut() ).thenReturn( "" );
        task.prepareTemplate( template );
    }


    @Test( expected = ResourceHostException.class )
    public void testIsTemplateExists() throws Exception
    {
        assertTrue( task.templateExists( template ) );

        when( commandResult.getStdOut() ).thenReturn( "" );

        assertFalse( task.templateExists( template ) );

        doThrow( new CommandException( "" ) ).when( resourceHost ).execute( any( RequestBuilder.class ) );

        task.templateExists( template );
    }


    @Test( expected = ResourceHostException.class )
    public void testImportTemplate() throws Exception
    {
        task.importTemplate( template );

        verify( commandUtil ).execute( any( RequestBuilder.class ), eq( resourceHost ) );

        doThrow( new CommandException( "" ) ).when( commandUtil )
                                             .execute( any( RequestBuilder.class ), eq( resourceHost ) );

        task.importTemplate( template );
    }


    @Test
    @Ignore
    public void testCall() throws Exception
    {
        task.call();

        //        verify( containerHost ).setDefaultGateway( GATEWAY );

        CreateContainerTask task = new CreateContainerTask( hostRegistry, resourceHost, template, HOSTNAME, CIDR, VLAN,
                TIMEOUT/*, ENV_ID*/ );
        task.commandUtil = commandUtil;

        task.call();

        //        verify( containerHost, times( 2 ) ).setDefaultGateway( GATEWAY );
    }
}
