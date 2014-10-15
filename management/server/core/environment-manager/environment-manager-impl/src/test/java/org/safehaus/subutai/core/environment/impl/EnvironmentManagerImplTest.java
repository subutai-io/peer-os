package org.safehaus.subutai.core.environment.impl;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.CloneContainersMessage;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.container.api.container.ContainerManager;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;
import org.safehaus.subutai.core.environment.impl.builder.EnvironmentBuilder;
import org.safehaus.subutai.core.environment.impl.dao.EnvironmentDAO;
import org.safehaus.subutai.core.network.api.NetworkManager;
import org.safehaus.subutai.core.peer.command.dispatcher.api.PeerCommandDispatcher;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Created by bahadyr on 9/25/14.
 */
@Ignore
@RunWith( MockitoJUnitRunner.class )
public class EnvironmentManagerImplTest
{

    private static final String HOSTNAME = "hostname";
    EnvironmentManagerImpl manager;
    @Mock
    ContainerManager containerManager;
    @Mock
    AgentManager agentManager;
    @Mock
    EnvironmentBuilder environmentBuilder;
    @Mock
    EnvironmentDAO environmentDao;
    @Mock
    NetworkManager networkManager;
    @Mock
    PeerCommandDispatcher pcd;
    @Mock
    TemplateRegistry registry;
    @Mock
    PeerCommandDispatcher peerCommandDispatcher;
    @Mock
    EnvironmentBlueprint environmentBlueprint;
    @Mock
    DataSource dataSource;


    @Before
    public void setUp() throws Exception
    {

        manager = new EnvironmentManagerImpl( dataSource );
        manager.setAgentManager( agentManager );
        manager.setContainerManager( containerManager );
        manager.setEnvironmentBuilder( environmentBuilder );
        manager.setEnvironmentDAO( environmentDao );
        manager.setNetworkManager( networkManager );
        manager.setPeerCommandDispatcher( pcd );
        manager.setTemplateRegistry( registry );
        manager.setPeerCommandDispatcher( peerCommandDispatcher );
    }


    @Test
    public void shoudBuildEnvironment() throws Exception
    {
        EnvironmentBuildProcess process = mock( EnvironmentBuildProcess.class );
        when( process.getEnvironmentBlueprint() ).thenReturn( environmentBlueprint );
        when( process.getUuid() ).thenReturn( UUIDUtil.generateTimeBasedUUID() );

        Map<String, CloneContainersMessage> map = new HashMap<>();
        CloneContainersMessage ccm = mock( CloneContainersMessage.class );
        when( ccm.isSuccess() ).thenReturn( true );
        Set<Agent> agents = new HashSet<>();

        Agent agent = mock( Agent.class );
        agent.setHostname( HOSTNAME );
        agents.add( agent );

        when( ccm.getResult() ).thenReturn( agents );
        map.put( "key", ccm );

        when( ccm.getNumberOfNodes() ).thenReturn( agents.size() );
        when( process.getMessageMap() ).thenReturn( map );
        manager.buildEnvironment( process );
    }
}
