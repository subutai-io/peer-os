package org.safehaus.subutai.core.lxc.quota.impl;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.core.lxc.quota.api.QuotaEnum;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;


public class QuotaManagerImplTest
{
    QuotaEnum parameter = QuotaEnum.MEMORY_LIMIT_IN_BYTES;
    QuotaManagerImpl quotaManager;
    CommandRunner commandRunner;
    String expectedValue = "200000000";
    Agent agent;


    @Before
    public void setupClasses()
    {
        commandRunner = mock( CommandRunner.class );
        agent = mock( Agent.class );
        AgentResult agentResult = mock( AgentResult.class );
        Command command = mock( Command.class );
        Map<UUID, AgentResult> resultsMap = new HashMap<>();
        Map<UUID, AgentResult> spy = spy( resultsMap );

        when( spy.get( any( UUID.class ) ) ).thenReturn( agentResult );

        when( agentResult.getStdOut() ).thenReturn( expectedValue );

        when( command.hasSucceeded() ).thenReturn( true );
        when( command.getResults() ).thenReturn( spy );

        when( commandRunner.createCommand( any( RequestBuilder.class ), anySetOf( Agent.class ) ) )
                .thenReturn( command );

        quotaManager = new QuotaManagerImpl( commandRunner );
    }


    @Test
    public void testSetQuota() throws Exception
    {
        quotaManager.setQuota( "containerName", parameter, expectedValue, agent );
        String value = quotaManager.getQuota( "containerName", parameter, agent );
        assertEquals( expectedValue, value );
    }


    @Test
    public void testGetQuota() throws Exception
    {
        String value = quotaManager.getQuota( "containerName", parameter, agent );
        quotaManager.setQuota( "containerName", parameter, "23423412342", agent );
        assertNotEquals( value, "23423412342" );
    }
}