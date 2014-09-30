package org.safehaus.subutai.core.environment.impl.builder;


import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.EnvironmentBuildTask;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.network.api.NetworkManager;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;

import static org.mockito.Mockito.mock;


/**
 * Created by bahadyr on 9/25/14.
 */
public class EnvironmentBuilderTest
{
    EnvironmentBuilder environmentBuilder;
    TemplateRegistry templateRegistry;
    AgentManager agentManager;
    NetworkManager networkManager;


    @Before
    public void init()
    {
        environmentBuilder = mock( EnvironmentBuilder.class );
        agentManager = mock( AgentManager.class );
        networkManager = mock( NetworkManager.class );
        environmentBuilder = new EnvironmentBuilder( templateRegistry, agentManager, networkManager );
    }


    @Test
    public void test()
    {


//         environmentBuilder.build(  )
    }
}
