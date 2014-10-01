package org.safehaus.subutai.core.apt.cli;


import org.junit.Test;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.apt.api.AptRepoException;
import org.safehaus.subutai.core.apt.api.AptRepositoryManager;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test for AddPackageCommand
 */
public class AddPackageCommandTest
{

    private static final String PACKAGE_PATH = "package/path";


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullAptRepoManager()
    {
        new AddPackageCommand( null, mock( AgentManager.class ) );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullAgentManager()
    {
        new AddPackageCommand( mock( AptRepositoryManager.class ), null );
    }


    @Test
    public void shouldAddPackage() throws AptRepoException
    {
        Agent agent = mock( Agent.class );
        AgentManager agentManager = mock( AgentManager.class );
        when( agentManager.getAgentByHostname( Common.MANAGEMENT_AGENT_HOSTNAME ) ).thenReturn( agent );
        AptRepositoryManager aptRepositoryManager = mock( AptRepositoryManager.class );

        AddPackageCommand addPackageCommand = new AddPackageCommand( aptRepositoryManager, agentManager );
        addPackageCommand.setPackagePath( PACKAGE_PATH );
        addPackageCommand.doExecute();

        verify( aptRepositoryManager ).addPackageByPath( eq( agent ), eq( PACKAGE_PATH ), eq( false ) );
    }
}
