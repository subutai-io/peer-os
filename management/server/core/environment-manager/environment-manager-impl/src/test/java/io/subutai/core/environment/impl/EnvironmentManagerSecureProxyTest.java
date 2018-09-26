package io.subutai.core.environment.impl;


import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.subutai.common.environment.Environment;
import io.subutai.common.environment.Node;
import io.subutai.common.environment.Topology;
import io.subutai.common.security.SshEncryptionType;
import io.subutai.common.security.relation.RelationInfoManager;
import io.subutai.common.security.relation.RelationLink;
import io.subutai.common.security.relation.RelationManager;
import io.subutai.common.security.relation.model.RelationInfoMeta;
import io.subutai.core.environment.api.EnvironmentEventListener;
import io.subutai.core.environment.impl.dao.EnvironmentService;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.LocalEnvironment;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.systemmanager.api.SystemManager;
import io.subutai.core.template.api.TemplateManager;
import io.subutai.core.tracker.api.Tracker;
import io.subutai.bazaar.share.common.BazaaarAdapter;
import io.subutai.bazaar.share.quota.ContainerQuota;
import io.subutai.bazaar.share.quota.ContainerSize;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class EnvironmentManagerSecureProxyTest
{
    EnvironmentManagerSecureProxy proxy;

    @Mock
    EnvironmentManagerImpl environmentManager;
    @Mock
    PeerManager peerManager;
    @Mock
    SecurityManager securityManager;
    @Mock
    IdentityManager identityManager;
    @Mock
    BazaaarAdapter bazaaarAdapter;
    @Mock
    EnvironmentService environmentService;
    @Mock
    Tracker tracker;
    @Mock
    TemplateManager templateManager;
    @Mock
    RelationManager relationManager;
    LocalEnvironment environment = TestHelper.ENVIRONMENT();
    @Mock
    Topology topology;
    EnvironmentContainerImpl environmentContainer = TestHelper.ENV_CONTAINER();

    @Mock
    SystemManager systemManager;


    class EnvironmentManagerSecureProxySUT extends EnvironmentManagerSecureProxy
    {
        public EnvironmentManagerSecureProxySUT( final PeerManager peerManager, final SecurityManager securityManager,
                                                 final IdentityManager identityManager, final Tracker tracker,
                                                 final RelationManager relationManager, final BazaaarAdapter bazaaarAdapter,
                                                 final EnvironmentService environmentService,
                                                 final SystemManager systemManager )
        {
            super( templateManager, peerManager, securityManager, identityManager, tracker, relationManager,
                    bazaaarAdapter,
                    environmentService, systemManager );
        }


        protected EnvironmentManagerImpl getEnvironmentManager( TemplateManager templateManager,
                                                                PeerManager peerManager,
                                                                SecurityManager securityManager, BazaaarAdapter bazaaarAdapter,
                                                                EnvironmentService environmentService,
                                                                SystemManager systemManager )
        {
            return environmentManager;
        }
    }


    @Mock
    EnvironmentEventListener environmentEventListener;


    @Before
    public void setUp() throws Exception
    {
        proxy = spy( new EnvironmentManagerSecureProxySUT( peerManager, securityManager, identityManager, tracker,
                relationManager, bazaaarAdapter, environmentService, systemManager ) );

        doNothing().when( proxy ).check( any( RelationLink.class ), any( RelationLink.class ), anyMap() );
        doNothing().when( proxy ).check( any( RelationLink.class ), any( Collection.class ), anyMap() );
        doReturn( Sets.newHashSet( environment ) ).when( environmentManager ).getEnvironments();
        doReturn( environmentContainer ).when( environment ).getContainerHostById( TestHelper.CONTAINER_ID );
        doReturn( environment ).when( environmentManager ).loadEnvironment( TestHelper.ENV_ID );
    }


    @Test
    public void testDispose() throws Exception
    {
        proxy.dispose();

        verify( environmentManager ).dispose();
    }


    @Test
    public void testRegisterListener() throws Exception
    {
        proxy.registerListener( environmentEventListener );

        verify( environmentManager ).registerListener( environmentEventListener );
    }


    @Test
    public void testUnregisterListener() throws Exception
    {
        proxy.unregisterListener( environmentEventListener );

        verify( environmentManager ).unregisterListener( environmentEventListener );
    }


    @Test
    public void testTraitsBuilder() throws Exception
    {
        Map<String, String> map = proxy.traitsBuilder( "a=b;c=d" );

        assertEquals( map.get( "a" ), "b" );
    }


    @Test
    public void testCheck() throws Exception
    {
        doCallRealMethod().when( proxy ).check( any( RelationLink.class ), any( RelationLink.class ), anyMap() );
        doCallRealMethod().when( proxy ).check( any( RelationLink.class ), any( Collection.class ), anyMap() );
        RelationLink source = mock( RelationLink.class );
        RelationLink target = mock( RelationLink.class );
        Map<String, String> traits = Maps.newHashMap();
        traits.put( "a", "b" );
        RelationInfoManager relationInfoManager = mock( RelationInfoManager.class );
        doReturn( relationInfoManager ).when( relationManager ).getRelationInfoManager();

        proxy.check( null, target, traits );

        verify( relationInfoManager )
                .checkRelation( eq( target ), isA( RelationInfoMeta.class ), isNull( String.class ) );

        proxy.check( source, target, traits );

        verify( relationInfoManager )
                .checkRelation( eq( source ), eq( target ), isA( RelationInfoMeta.class ), isNull( String.class ) );
    }


    @Test
    public void testCheck2() throws Exception
    {
        doCallRealMethod().when( proxy ).check( any( RelationLink.class ), any( RelationLink.class ), anyMap() );
        doCallRealMethod().when( proxy ).check( any( RelationLink.class ), any( Collection.class ), anyMap() );
        RelationLink source = mock( RelationLink.class );
        RelationLink target = mock( RelationLink.class );
        Collection<? extends RelationLink> targets = Collections.singleton( target );
        Map<String, String> traits = Maps.newHashMap();
        traits.put( "a", "b" );
        RelationInfoManager relationInfoManager = mock( RelationInfoManager.class );
        doReturn( relationInfoManager ).when( relationManager ).getRelationInfoManager();

        proxy.check( source, targets, traits );

        verify( proxy ).check( source, target, traits );
    }


    @Test
    public void testGetEnvironments() throws Exception
    {
        Set<Environment> environmentSet = proxy.getEnvironments();

        assertTrue( environmentSet.contains( environment ) );
    }


    @Test
    public void testGetEnvironmentsByOwnerId() throws Exception
    {
        proxy.getEnvironmentsByOwnerId( 123L );

        verify( environmentManager ).getEnvironmentsByOwnerId( 123L );
    }


    @Test
    public void testCreateEnvironment() throws Exception
    {
        doReturn( "ENV" ).when( topology ).getEnvironmentName();
        Map<String, Set<Node>> placement = mock( Map.class );
        doReturn( placement ).when( topology ).getNodeGroupPlacement();
        doReturn( false ).when( placement ).isEmpty();

        proxy.createEnvironment( topology, true );

        verify( environmentManager ).createEnvironment( topology, true );
    }


    @Test
    public void testModifyEnvironment() throws Exception
    {
        Map<String, ContainerQuota> changedContainers = Maps.newHashMap();
        changedContainers.put( TestHelper.CONTAINER_ID, new ContainerQuota( ContainerSize.LARGE ) );

        proxy.modifyEnvironment( TestHelper.ENV_ID, topology, Sets.newHashSet( TestHelper.CONTAINER_ID ),
                changedContainers, true );

        verify( environmentManager )
                .modifyEnvironment( TestHelper.ENV_ID, topology, Sets.newHashSet( TestHelper.CONTAINER_ID ),
                        changedContainers, true );
    }


    @Test
    public void testGrowEnvironment() throws Exception
    {
        proxy.growEnvironment( TestHelper.ENV_ID, topology, true );

        verify( environmentManager ).growEnvironment( TestHelper.ENV_ID, topology, true );
    }


    @Test
    public void testAddSshKey() throws Exception
    {
        proxy.addSshKey( TestHelper.ENV_ID, TestHelper.SSH_KEY, true );

        verify( environmentManager ).addSshKey( TestHelper.ENV_ID, TestHelper.SSH_KEY, true );
    }


    @Test
    public void testRemoveSshKey() throws Exception
    {
        proxy.removeSshKey( TestHelper.ENV_ID, TestHelper.SSH_KEY, true );

        verify( environmentManager ).removeSshKey( TestHelper.ENV_ID, TestHelper.SSH_KEY, true );
    }


    @Test
    public void testGetSshKeys() throws Exception
    {
        proxy.getSshKeys( TestHelper.ENV_ID, SshEncryptionType.RSA );

        verify( environmentManager ).getSshKeys( TestHelper.ENV_ID, SshEncryptionType.RSA );
    }


    @Test
    public void testCreateSshKey() throws Exception
    {
        proxy.createSshKey( TestHelper.ENV_ID, TestHelper.HOSTNAME, SshEncryptionType.RSA );

        verify( environmentManager ).createSshKey( TestHelper.ENV_ID, TestHelper.HOSTNAME, SshEncryptionType.RSA );
    }


    @Test
    public void testResetP2PSecretKey() throws Exception
    {
        proxy.resetP2PSecretKey( TestHelper.ENV_ID, "SECRET", 123L, true );

        verify( environmentManager ).resetP2PSecretKey( TestHelper.ENV_ID, "SECRET", 123L, true );
    }


    @Test
    public void testDestroyEnvironment() throws Exception
    {
        proxy.destroyEnvironment( TestHelper.ENV_ID, false );

        verify( environmentManager ).destroyEnvironment( TestHelper.ENV_ID, false );
    }


    @Test
    public void testDestroyContainer() throws Exception
    {
        proxy.destroyContainer( TestHelper.ENV_ID, TestHelper.CONTAINER_ID, true );

        verify( environmentManager ).destroyContainer( TestHelper.ENV_ID, TestHelper.CONTAINER_ID, true );
    }


    @Test
    public void testCancelEnvironmentWorkflow() throws Exception
    {
        proxy.cancelEnvironmentWorkflow( TestHelper.ENV_ID );

        verify( environmentManager ).cancelEnvironmentWorkflow( TestHelper.ENV_ID );
    }


    @Test
    public void testGetActiveWorkflows() throws Exception
    {
        proxy.getActiveWorkflows();

        verify( environmentManager ).getActiveWorkflows();
    }


    @Test
    public void testLoadEnvironment() throws Exception
    {
        proxy.loadEnvironment( TestHelper.ENV_ID );

        verify( environmentManager, atLeastOnce() ).loadEnvironment( TestHelper.ENV_ID );
    }
}
