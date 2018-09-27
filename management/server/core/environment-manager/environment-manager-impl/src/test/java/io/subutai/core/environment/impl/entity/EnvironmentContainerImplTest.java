package io.subutai.core.environment.impl.entity;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Sets;

import io.subutai.common.command.CommandCallback;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.environment.Environment;
import io.subutai.common.host.ContainerHostInfoModel;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostArchitecture;
import io.subutai.common.host.HostId;
import io.subutai.common.host.HostInterface;
import io.subutai.common.host.HostInterfaceModel;
import io.subutai.common.host.HostInterfaces;
import io.subutai.common.host.NullHostInterface;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.protocol.Template;
import io.subutai.common.settings.Common;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.adapter.EnvironmentAdapter;
import io.subutai.bazaar.share.quota.ContainerQuota;
import io.subutai.bazaar.share.quota.ContainerSize;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class EnvironmentContainerImplTest
{
    private static final String INITIATOR_ID = "123";
    private static final String PEER_ID = "123";
    private static final String CONTAINER_ID = "123";
    private static final String RH_ID = "321";
    private static final String TEMPLATE_ID = "321";
    private static final String ENV_ID = "321";
    private static final String HOSTNAME = "container1";
    private static final ContainerHostState STATE = ContainerHostState.RUNNING;
    private static final String TAG = "tag";
    private static final int VLAN = 123;


    private EnvironmentContainerImpl environmentContainer;

    @Mock
    LocalEnvironment environment;
    @Mock
    EnvironmentManagerImpl environmentManager;
    @Mock
    EnvironmentAdapter environmentAdapter;
    @Mock
    LocalPeer peer;
    @Mock
    Template template;
    @Mock
    CommandCallback CALLBACK;
    @Mock
    RequestBuilder COMMAND;
    @Mock
    ContainerQuota QUOTA;


    @Before
    public void setUp() throws Exception
    {
        HostInterfaces hostInterfaces = new HostInterfaces( CONTAINER_ID,
                Sets.newHashSet( new HostInterfaceModel( Common.DEFAULT_CONTAINER_INTERFACE, Common.LOCAL_HOST_IP ) ) );

        ContainerHostInfoModel containerHostInfoModel =
                new ContainerHostInfoModel( CONTAINER_ID, HOSTNAME, HOSTNAME, hostInterfaces, HostArchitecture.AMD64,
                        STATE, ENV_ID, VLAN );

        environmentContainer =
                spy( new EnvironmentContainerImpl( INITIATOR_ID, PEER_ID, containerHostInfoModel, TEMPLATE_ID,
                        Common.DEFAULT_DOMAIN_NAME, new ContainerQuota( ContainerSize.SMALL ), RH_ID ) );

        doReturn( peer ).when( environmentContainer ).getLocalPeer();
        doReturn( template ).when( peer ).getTemplateById( TEMPLATE_ID );

        environmentContainer.setEnvironmentManager( environmentManager );
        environmentContainer.setEnvironment( environment );

        doReturn( peer ).when( environmentManager ).resolvePeer( anyString() );
        doReturn( PEER_ID ).when( peer ).getId();
        doReturn( ENV_ID ).when( environment ).getId();
        doReturn( STATE ).when( peer ).getContainerState( any( ContainerId.class ) );
        doReturn( Sets.newHashSet() ).when( environment ).getContainerHostsByPeerId( PEER_ID );

        doNothing().when( environmentContainer ).validateTrustChain();
    }


    @Test
    public void getResourceHostId() throws Exception
    {
        HostId rhId = environmentContainer.getResourceHostId();

        assertNotNull( rhId );
        assertEquals( RH_ID, rhId.getId() );
    }


    @Test
    public void testIsLocal() throws Exception
    {
        environmentContainer.isLocal();

        verify( environmentContainer ).getLocalPeer();
        verify( environmentContainer ).getPeerId();
    }


    @Test
    public void testGetEnvironmentId() throws Exception
    {
        assertEquals( CONTAINER_ID, environmentContainer.getId() );
    }


    @Test
    public void testGetState() throws Exception
    {
        assertEquals( STATE, environmentContainer.getState() );
    }


    @Test
    public void testGetContainerName() throws Exception
    {
        assertEquals( HOSTNAME, environmentContainer.getContainerName() );
    }


    @Test
    public void testGetHostName() throws Exception
    {
        assertEquals( HOSTNAME, environmentContainer.getHostname() );
    }


    @Test
    public void testDestroy() throws Exception
    {
        environmentContainer.destroy( false );

        verify( peer ).destroyContainer( any( ContainerId.class ) );
        verify( environment ).removeEnvironmentPeer( PEER_ID );
        verify( environmentManager ).notifyOnContainerDestroyed( any( Environment.class ), eq( CONTAINER_ID ) );
    }


    @Test
    public void testStart() throws Exception
    {
        environmentContainer.start();

        verify( peer ).startContainer( environmentContainer.getContainerId() );
    }


    @Test
    public void testStop() throws Exception
    {
        environmentContainer.stop();

        verify( peer ).stopContainer( environmentContainer.getContainerId() );
    }


    @Test
    public void testGetTemplate() throws Exception
    {
        assertEquals( template, environmentContainer.getTemplate() );

        verify( peer ).getTemplateById( TEMPLATE_ID );
    }


    @Test
    public void testGetTemplateName() throws Exception
    {
        environmentContainer.getTemplateName();

        verify( environmentContainer ).getTemplate();
    }


    @Test
    public void testGetTemplateId() throws Exception
    {
        assertEquals( TEMPLATE_ID, environmentContainer.getTemplateId() );
    }


    @Test
    public void addTag() throws Exception
    {
        environmentContainer.addTag( TAG );

        verify( environmentManager ).update( environmentContainer );
    }


    @Test
    public void removeTag() throws Exception
    {
        environmentContainer.removeTag( TAG );

        verify( environmentManager ).update( environmentContainer );
    }


    @Test
    public void testGetTags() throws Exception
    {
        environmentContainer.addTag( TAG );

        assertTrue( environmentContainer.getTags().contains( TAG ) );
    }


    @Test
    public void testGetPeerId() throws Exception
    {
        assertEquals( PEER_ID, environmentContainer.getPeerId() );
    }


    @Test
    public void testGetId() throws Exception
    {
        assertEquals( CONTAINER_ID, environmentContainer.getId() );
    }


    @Test
    public void testSetHostname() throws Exception
    {
        environmentContainer.setHostname( "NEWHOSTNAME", false );

        verify( peer ).setContainerHostname( environmentContainer.getContainerId(), "NEWHOSTNAME" );
    }


    @Test
    public void testExecute() throws Exception
    {
        environmentContainer.execute( COMMAND );

        verify( peer ).execute( COMMAND, environmentContainer );
    }


    @Test
    public void testExecuteCallback() throws Exception
    {
        environmentContainer.execute( COMMAND, CALLBACK );

        verify( peer ).execute( COMMAND, environmentContainer, CALLBACK );
    }


    @Test
    public void testExecuteAsync() throws Exception
    {
        environmentContainer.executeAsync( COMMAND );

        verify( peer ).executeAsync( COMMAND, environmentContainer );
    }


    @Test
    public void testExecuteAsyncCallback() throws Exception
    {
        environmentContainer.executeAsync( COMMAND, CALLBACK );

        verify( peer ).executeAsync( COMMAND, environmentContainer, CALLBACK );
    }


    @Test
    public void testIsConnected() throws Exception
    {

        assertTrue( environmentContainer.isConnected() );
    }


    @Test
    public void testGetHostInterfaces() throws Exception
    {
        HostInterface hostInterface = environmentContainer.getHostInterfaces().findByIp( Common.LOCAL_HOST_IP );

        assertFalse( hostInterface instanceof NullHostInterface );
        assertNotNull( hostInterface );
    }


    @Test
    public void testGetInterfaceByName() throws Exception
    {

        HostInterface hostInterface = environmentContainer.getInterfaceByName( Common.DEFAULT_CONTAINER_INTERFACE );

        assertFalse( hostInterface instanceof NullHostInterface );
        assertNotNull( hostInterface );
    }


    @Test
    public void testGetArch() throws Exception
    {
        assertEquals( HostArchitecture.AMD64, environmentContainer.getArch() );
    }


    @Test
    public void testGetQuota() throws Exception
    {
        environmentContainer.getQuota();

        verify( peer ).getQuota( environmentContainer.getContainerId() );
    }


    @Test
    public void testSetQuota() throws Exception
    {
        environmentContainer.setQuota( QUOTA );

        verify( peer ).setQuota( environmentContainer.getContainerId(), QUOTA );
    }


    @Test
    public void testGetContainerSize() throws Exception
    {
        assertEquals( ContainerSize.SMALL, environmentContainer.getContainerSize() );
    }


    //    @Test
    //    public void testSetContainerSize() throws Exception
    //    {
    //        environmentContainer.setContainerQuota( ContainerSize.HUGE );
    //
    //        verify( peer ).setContainerQuota( environmentContainer.getContainerId(), ContainerSize.HUGE );
    //    }


    @Test
    public void testGetInitiatorPeerId() throws Exception
    {
        assertEquals( INITIATOR_ID, environmentContainer.getInitiatorPeerId() );
    }


    @Test
    public void testGetAuthorizedKeys() throws Exception
    {
        environmentContainer.getAuthorizedKeys();

        verify( peer ).getContainerAuthorizedKeys( environmentContainer.getContainerId() );
    }


    @Test
    public void testGetEnvironment() throws Exception
    {
        assertEquals( environment, environmentContainer.getEnvironment() );
    }


    @Test
    public void testGetIp() throws Exception
    {
        assertEquals( Common.LOCAL_HOST_IP, environmentContainer.getIp() );
    }
}
