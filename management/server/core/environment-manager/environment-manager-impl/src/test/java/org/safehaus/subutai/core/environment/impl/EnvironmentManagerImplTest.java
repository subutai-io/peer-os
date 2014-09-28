package org.safehaus.subutai.core.environment.impl;


import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.protocol.PeerCommandMessage;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.container.api.container.ContainerManager;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.environment.api.EnvironmentContainer;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;
import org.safehaus.subutai.core.environment.api.helper.ProcessStatusEnum;
import org.safehaus.subutai.core.environment.impl.builder.EnvironmentBuilder;
import org.safehaus.subutai.core.environment.impl.dao.EnvironmentDAO;
import org.safehaus.subutai.core.network.api.NetworkManager;
import org.safehaus.subutai.core.peer.command.dispatcher.api.PeerCommandDispatcher;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;

import static org.mockito.Mockito.mock;


/**
 * Created by bahadyr on 9/25/14.
 */
@RunWith( MockitoJUnitRunner.class )
public class EnvironmentManagerImplTest
{

    EnvironmentManagerImpl environmentManager;
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
        environmentManager = new EnvironmentManagerImpl();
        environmentManager.setAgentManager( agentManager );
        environmentManager.setContainerManager( containerManager );
        environmentManager.setDbManager( dbManager );
        environmentManager.setEnvironmentBuilder( environmentBuilder );
        environmentManager.setEnvironmentDAO( environmentDao );
        environmentManager.setNetworkManager( networkManager );
        environmentManager.setPeerCommandDispatcher( pcd );
        environmentManager.setTemplateRegistry( registry );
        environmentManager.setPeerCommandDispatcher( peerCommandDispatcher );
    }


    @After
    public void shouldDestroy()
    {
        environmentManager.destroy();
    }


    @Test
    public void shouldAddContainer()
    {
        environmentManager.addContainer( mock( EnvironmentContainer.class ) );
    }


    @Test
    public void testNotifier() throws EnvironmentBuildException
    {
        EnvironmentBuildProcess process = createEnvironmentBuildProcess();
        environmentManager.buildEnvironment( process );
        PeerCommandMessage pcm = getPeerCommandMessage();
    }


    private Void getNewPeerCommandMessage()
    {
        return null;
    }


    private PeerCommandMessage getPeerCommandMessage()
    {
        return new PeerCommandMessage()
        {
            @Override
            public void setResult( final Object result )
            {

            }


            @Override
            public Object getResult()
            {
                return null;
            }
        };
    }


    private EnvironmentBuildProcess createEnvironmentBuildProcess()
    {
        EnvironmentBuildProcess ebp = new EnvironmentBuildProcess( "Environment name" );
        ebp.setProcessStatusEnum( ProcessStatusEnum.NEW_PROCESS );
        ebp.setCompleteStatus( false );
        ebp.setUuid( UUID.randomUUID() );

        return ebp;
    }
}
