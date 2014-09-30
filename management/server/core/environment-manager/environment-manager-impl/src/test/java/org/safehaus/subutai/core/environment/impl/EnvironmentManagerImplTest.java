package org.safehaus.subutai.core.environment.impl;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.protocol.CloneContainersMessage;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.container.api.container.ContainerManager;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;
import org.safehaus.subutai.core.environment.impl.builder.EnvironmentBuilder;
import org.safehaus.subutai.core.environment.impl.dao.EnvironmentDAO;
import org.safehaus.subutai.core.network.api.NetworkManager;
import org.safehaus.subutai.core.peer.command.dispatcher.api.PeerCommandDispatcher;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;


/**
 * Created by bahadyr on 9/25/14.
 */
@RunWith(MockitoJUnitRunner.class)
public class EnvironmentManagerImplTest
{

    EnvironmentManagerImpl manager;
    @Mock
    ContainerManager containerManager;
    @Mock
    AgentManager agentManager;
    @Mock
    DbManager dbManager;
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


    @Before
    public void init()
    {
        manager = new EnvironmentManagerImpl();
        manager.setAgentManager( agentManager );
        manager.setContainerManager( containerManager );
        manager.setDbManager( dbManager );
        manager.setEnvironmentBuilder( environmentBuilder );
        manager.setEnvironmentDAO( environmentDao );
        manager.setNetworkManager( networkManager );
        manager.setPeerCommandDispatcher( pcd );
        manager.setTemplateRegistry( registry );
        manager.setPeerCommandDispatcher( peerCommandDispatcher );
    }


    @Test
    public void testName() throws Exception
    {
        EnvironmentBuildProcess task = getEBT();
        //        manager.buildEnvironment( task );
    }


    String name = "name";
    UUID envId;
    UUID peerID;


    private EnvironmentBuildProcess getEBT()
    {
        EnvironmentBuildProcess process = new EnvironmentBuildProcess( name );

        envId = UUID.randomUUID();
        peerID = UUID.randomUUID();
        CloneContainersMessage ccm = new CloneContainersMessage( envId, peerID );
        process.putCloneContainerMessage( peerID.toString(), ccm );
        return process;
    }
}
