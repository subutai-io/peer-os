package org.safehaus.subutai.core.env.rest;


import java.util.UUID;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.environment.ContainerHostNotFoundException;
import org.safehaus.subutai.common.environment.Environment;
import org.safehaus.subutai.common.environment.EnvironmentModificationException;
import org.safehaus.subutai.common.environment.EnvironmentNotFoundException;
import org.safehaus.subutai.common.environment.EnvironmentStatus;
import org.safehaus.subutai.common.environment.Topology;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.peer.Peer;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.env.api.EnvironmentManager;
import org.safehaus.subutai.core.env.api.exception.EnvironmentCreationException;
import org.safehaus.subutai.core.env.api.exception.EnvironmentDestructionException;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;

import com.google.common.collect.Sets;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class RestServiceImplTest
{
    @Mock
    EnvironmentManager environmentManager;
    @Mock
    TemplateRegistry templateRegistry;
    @Mock
    PeerManager peerManager;
    @Mock
    Peer peer;
    @Mock
    Template template;
    @Mock
    Environment environment;
    @Mock
    ContainerHost containerHost;

    RestServiceImpl restService;


    @Before
    public void setUp() throws Exception
    {
        restService = new RestServiceImpl( environmentManager, peerManager, templateRegistry );
        when( peerManager.getPeer( TestUtil.PEER_ID ) ).thenReturn( peer );
        when( templateRegistry.getTemplate( TestUtil.TEMPLATE_NAME ) ).thenReturn( template );
        when( environmentManager
                .createEnvironment( anyString(), any( Topology.class ), anyString(), anyString(), anyBoolean() ) )
                .thenReturn( environment );
        when( environment.getId() ).thenReturn( TestUtil.ENV_ID );
        when( environment.getName() ).thenReturn( TestUtil.ENV_NAME );
        when( environment.getStatus() ).thenReturn( EnvironmentStatus.HEALTHY );
        when( environment.getContainerHosts() ).thenReturn( Sets.newHashSet( containerHost ) );
        when( containerHost.getId() ).thenReturn( TestUtil.CONTAINER_ID );
        when( containerHost.getEnvironmentId() ).thenReturn( TestUtil.ENV_ID.toString() );
        when( containerHost.getHostname() ).thenReturn( TestUtil.HOSTNAME );
        when( containerHost.getIpByInterfaceName( anyString() ) ).thenReturn( TestUtil.IP );
        when( containerHost.getTemplateName() ).thenReturn( TestUtil.TEMPLATE_NAME );
        when( containerHost.getState() ).thenReturn( TestUtil.CONTAINER_STATE );
        when( environmentManager.getEnvironments() ).thenReturn( Sets.newHashSet( environment ) );
        when( environmentManager.findEnvironment( TestUtil.ENV_ID ) ).thenReturn( environment );
        when( environment.getContainerHostById( TestUtil.CONTAINER_ID ) ).thenReturn( containerHost );
    }


    private void throwEnvironmentException() throws EnvironmentCreationException, EnvironmentNotFoundException

    {
        doThrow( new EnvironmentCreationException( "" ) ).when( environmentManager )
                                                         .createEnvironment( anyString(), any( Topology.class ),
                                                                 anyString(), anyString(), anyBoolean() );

        doThrow( new EnvironmentNotFoundException( "" ) ).when( environmentManager )
                                                         .findEnvironment( any( UUID.class ) );
    }


    @Test
    public void testCreateEnvironment() throws Exception
    {
        Response response = restService
                .createEnvironment( TestUtil.ENV_NAME, TestUtil.TOPOLOGY_JSON, TestUtil.SUBNET, TestUtil.SSH_KEY );

        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );

        response = restService.createEnvironment( TestUtil.ENV_NAME,
                TestUtil.TOPOLOGY_JSON.replace( TestUtil.PEER_ID.toString(), "" ), TestUtil.SUBNET, TestUtil.SSH_KEY );

        assertEquals( Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus() );

        throwEnvironmentException();

        response = restService
                .createEnvironment( TestUtil.ENV_NAME, TestUtil.TOPOLOGY_JSON, TestUtil.SUBNET, TestUtil.SSH_KEY );

        assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus() );
    }


    @Test
    public void testGetContainerEnvironmentId() throws Exception
    {
        Response response = restService.getContainerEnvironmentId( TestUtil.CONTAINER_ID.toString() );

        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );

        response = restService.getContainerEnvironmentId( UUID.randomUUID().toString() );

        assertEquals( Response.Status.NOT_FOUND.getStatusCode(), response.getStatus() );

        response = restService.getContainerEnvironmentId( "" );

        assertEquals( Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus() );
    }


    @Test
    public void testGetDefaultDomain() throws Exception
    {

        Response response = restService.getDefaultDomainName();

        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
    }


    @Test
    public void testListEnvironments() throws Exception
    {

        Response response = restService.listEnvironments();

        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
    }


    @Test
    public void testViewEnvironment() throws Exception
    {
        Response response = restService.viewEnvironment( TestUtil.ENV_ID.toString() );

        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );

        response = restService.viewEnvironment( "" );

        assertEquals( Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus() );

        throwEnvironmentException();

        response = restService.viewEnvironment( TestUtil.ENV_ID.toString() );

        assertEquals( Response.Status.NOT_FOUND.getStatusCode(), response.getStatus() );
    }


    @Test
    public void testDestroyEnvironment() throws Exception
    {
        Response response = restService.destroyEnvironment( TestUtil.ENV_ID.toString() );

        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );

        response = restService.destroyEnvironment( "" );

        assertEquals( Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus() );

        doThrow( new EnvironmentNotFoundException( "" ) ).when( environmentManager )
                                                         .destroyEnvironment( any( UUID.class ), anyBoolean(),
                                                                 anyBoolean() );

        response = restService.destroyEnvironment( TestUtil.ENV_ID.toString() );

        assertEquals( Response.Status.NOT_FOUND.getStatusCode(), response.getStatus() );

        doThrow( new EnvironmentDestructionException( "" ) ).when( environmentManager )
                                                            .destroyEnvironment( any( UUID.class ), anyBoolean(),
                                                                    anyBoolean() );

        response = restService.destroyEnvironment( TestUtil.ENV_ID.toString() );

        assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus() );
    }


    @Test
    public void testDestroyContainer() throws Exception
    {

        Response response = restService.destroyContainer( TestUtil.CONTAINER_ID.toString() );

        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );

        response = restService.destroyContainer( "" );

        assertEquals( Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus() );

        response = restService.destroyContainer( UUID.randomUUID().toString() );

        assertEquals( Response.Status.NOT_FOUND.getStatusCode(), response.getStatus() );

        doThrow( new EnvironmentNotFoundException( "" ) ).when( environmentManager )
                                                         .destroyContainer( any( ContainerHost.class ), anyBoolean(),
                                                                 anyBoolean() );

        response = restService.destroyContainer( TestUtil.CONTAINER_ID.toString() );

        assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus() );
    }


    @Test
    public void testGrowEnvironment() throws Exception
    {
        Response response = restService.growEnvironment( TestUtil.ENV_ID.toString(), TestUtil.TOPOLOGY_JSON );

        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );

        response = restService.growEnvironment( "", TestUtil.TOPOLOGY_JSON );

        assertEquals( Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus() );

        response = restService.growEnvironment( TestUtil.ENV_ID.toString(),
                TestUtil.TOPOLOGY_JSON.replace( TestUtil.PEER_ID.toString(), "" ) );

        assertEquals( Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus() );

        doThrow( new EnvironmentNotFoundException( "" ) ).when( environmentManager )
                                                         .growEnvironment( any( UUID.class ), any( Topology.class ),
                                                                 anyBoolean() );

        response = restService.growEnvironment( TestUtil.ENV_ID.toString(), TestUtil.TOPOLOGY_JSON );

        assertEquals( Response.Status.NOT_FOUND.getStatusCode(), response.getStatus() );

        doThrow( new EnvironmentModificationException( "" ) ).when( environmentManager )
                                                             .growEnvironment( any( UUID.class ), any( Topology.class ),
                                                                     anyBoolean() );

        response = restService.growEnvironment( TestUtil.ENV_ID.toString(), TestUtil.TOPOLOGY_JSON );

        assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus() );
    }


    @Test
    public void testGetContainerState() throws Exception
    {
        Response response = restService.getContainerState( TestUtil.CONTAINER_ID.toString() );

        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );


        response = restService.getContainerState( "" );

        assertEquals( Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus() );

        doThrow( new ContainerHostNotFoundException( "" ) ).when( environment )
                                                           .getContainerHostById( TestUtil.CONTAINER_ID );

        response = restService.getContainerState( TestUtil.CONTAINER_ID.toString() );

        assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus() );

        response = restService.getContainerState( UUID.randomUUID().toString() );

        assertEquals( Response.Status.NOT_FOUND.getStatusCode(), response.getStatus() );
    }


    @Test
    public void testStartContainer() throws Exception
    {
        Response response = restService.startContainer( TestUtil.CONTAINER_ID.toString() );

        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );

        response = restService.startContainer( "" );

        assertEquals( Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus() );

        doThrow( new ContainerHostNotFoundException( "" ) ).when( environment )
                                                           .getContainerHostById( TestUtil.CONTAINER_ID );

        response = restService.startContainer( TestUtil.CONTAINER_ID.toString() );

        assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus() );

        response = restService.startContainer( UUID.randomUUID().toString() );

        assertEquals( Response.Status.NOT_FOUND.getStatusCode(), response.getStatus() );
    }


    @Test
    public void testStopContainer() throws Exception
    {
        Response response = restService.stopContainer( TestUtil.CONTAINER_ID.toString() );

        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );

        response = restService.stopContainer( "" );

        assertEquals( Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus() );

        doThrow( new ContainerHostNotFoundException( "" ) ).when( environment )
                                                           .getContainerHostById( TestUtil.CONTAINER_ID );

        response = restService.stopContainer( TestUtil.CONTAINER_ID.toString() );

        assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus() );

        response = restService.stopContainer( UUID.randomUUID().toString() );

        assertEquals( Response.Status.NOT_FOUND.getStatusCode(), response.getStatus() );
    }


    @Test
    public void testSetSshKey() throws Exception
    {
        Response response = restService.setSshKey( TestUtil.ENV_ID.toString(), TestUtil.SSH_KEY );

        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );

        doThrow( new EnvironmentNotFoundException( "" ) ).when( environmentManager )
                                                         .setSshKey( any( UUID.class ), anyString(), anyBoolean() );

        response = restService.setSshKey( TestUtil.ENV_ID.toString(), TestUtil.SSH_KEY );

        assertEquals( Response.Status.NOT_FOUND.getStatusCode(), response.getStatus() );

        doThrow( new EnvironmentModificationException( "" ) ).when( environmentManager )
                                                             .setSshKey( any( UUID.class ), anyString(), anyBoolean() );

        response = restService.setSshKey( TestUtil.ENV_ID.toString(), TestUtil.SSH_KEY );

        assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus() );

        response = restService.setSshKey( "", TestUtil.SSH_KEY );

        assertEquals( Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus() );

        response = restService.setSshKey( TestUtil.ENV_ID.toString(), null );

        assertEquals( Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus() );
    }


    @Test
    public void testRemoveSshKey() throws Exception
    {
        Response response = restService.removeSshKey( TestUtil.ENV_ID.toString() );

        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );

        response = restService.removeSshKey( "" );

        assertEquals( Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus() );

        doThrow( new EnvironmentNotFoundException( "" ) ).when( environmentManager )
                                                         .setSshKey( any( UUID.class ), anyString(), anyBoolean() );

        response = restService.removeSshKey( TestUtil.ENV_ID.toString() );

        assertEquals( Response.Status.NOT_FOUND.getStatusCode(), response.getStatus() );

        doThrow( new EnvironmentModificationException( "" ) ).when( environmentManager )
                                                             .setSshKey( any( UUID.class ), anyString(), anyBoolean() );

        response = restService.removeSshKey( TestUtil.ENV_ID.toString() );

        assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus() );
    }
}
