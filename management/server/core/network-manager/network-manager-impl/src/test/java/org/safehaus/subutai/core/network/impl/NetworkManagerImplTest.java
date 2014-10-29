package org.safehaus.subutai.core.network.impl;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.dispatcher.api.CommandDispatcher;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Test for NetworkManagerImpl
 */
public class NetworkManagerImplTest
{

    private static final String DOMAIN = "domain";
    private static final String ERR_MSG = "oops";
    private static final String KEY = "key";
    private CommandDispatcher commandDispatcher;
    private NetworkManagerImpl networkManager;
    private Command command;

    private static final String HOSTNAME1 = "hostname1";
    private static final String HOSTNAME2 = "hostname2";
    private static final String IP1 = "127.0.0.1";
    private static final String IP2 = "127.0.0.2";


    @Before
    public void setUp()
    {


        Map<UUID, AgentResult> results = new HashMap<>();
        AgentResult agentResult = mock( AgentResult.class );
        when( agentResult.getStdOut() ).thenReturn( KEY );

        commandDispatcher = mock( CommandDispatcher.class );
        command = mock( Command.class );
        when( command.hasSucceeded() ).thenReturn( true );
        when( command.hasCompleted() ).thenReturn( true );
        when( command.getResults() ).thenReturn( results );
        when( commandDispatcher.createCommand( any( RequestBuilder.class ), anySet() ) ).thenReturn( command );
        when( commandDispatcher.createContainerCommand( any( RequestBuilder.class ), anySet() ) ).thenReturn( command );
        when( commandDispatcher.createCommand( anyString(), any( RequestBuilder.class ), anySet() ) )
                .thenReturn( command );
        networkManager = new NetworkManagerImpl( commandDispatcher );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullCommandRunner()
    {

        new NetworkManagerImpl( null );
    }
}
