package org.safehaus.subutai.core.environment.impl;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;
import org.safehaus.subutai.core.environment.impl.dao.EnvironmentDAO;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Created by bahadyr on 9/25/14.
 */
@Ignore
@RunWith(MockitoJUnitRunner.class)
public class EnvironmentManagerImplTest
{

    private static final String HOSTNAME = "hostname";
    private static final String NAME = "name";
    EnvironmentManagerImpl manager;

    @Mock
    EnvironmentDAO environmentDao;
    @Mock
    SecurityManager securityManager;
    @Mock
    TemplateRegistry registry;
    @Mock
    EnvironmentBlueprint environmentBlueprint;
    @Mock
    DataSource dataSource;


    @Before
    public void setUp() throws Exception
    {
        manager = new EnvironmentManagerImpl( dataSource );
        manager.setEnvironmentDAO( environmentDao );
    }


    @Test
    public void shoudBuildEnvironment() throws Exception
    {
        EnvironmentBuildProcess process = mock( EnvironmentBuildProcess.class );
        when( process.getBlueprintId() ).thenReturn( UUID.randomUUID() );
        when( process.getId() ).thenReturn( UUIDUtil.generateTimeBasedUUID() );

        Map<String, CloneContainersMessage> map = new HashMap<>();
        CloneContainersMessage ccm = mock( CloneContainersMessage.class );

        Set<Agent> agents = new HashSet<>();

        Agent agent = mock( Agent.class );
        agent.setHostname( HOSTNAME );
        agents.add( agent );


        map.put( "key", ccm );

        when( ccm.getNumberOfNodes() ).thenReturn( agents.size() );
        when( process.getMessageMap() ).thenReturn( map );
        manager.buildEnvironment( process );
    }
}
