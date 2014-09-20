package org.safehaus.subutai.core.apt.cli;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.apt.api.AptRepoException;
import org.safehaus.subutai.core.apt.api.AptRepositoryManager;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


@Command(scope = "apt", name = "read-files", description = "Read files inside deb package")
public class ReadFilesCommand extends OsgiCommandSupport
{
    @Argument(index = 0, name = "package path", required = true, multiValued = false,
            description = "path to package")
    String packagePath;
    @Argument(index = 1, name = "file path(s)", required = true, multiValued = true,
            description = "relative file path(s) to read")
    Collection<String> filesPaths;

    private AptRepositoryManager aptRepositoryManager;
    private AgentManager agentManager;


    public void setAptRepositoryManager( final AptRepositoryManager aptRepositoryManager )
    {
        this.aptRepositoryManager = aptRepositoryManager;
    }


    public void setAgentManager( final AgentManager agentManager )
    {
        this.agentManager = agentManager;
    }


    @Override
    protected Object doExecute()
    {
        List<String> filePathsList = new ArrayList<>( filesPaths );
        try
        {
            List<String> fileContents = aptRepositoryManager
                    .readFileContents( agentManager.getAgentByHostname( Common.MANAGEMENT_AGENT_HOSTNAME ), packagePath,
                            filePathsList );
            for ( int i = 0; i < fileContents.size(); i++ )
            {
                final String content = fileContents.get( i );
                System.out.println( "File: " + filePathsList.get( i ) + " Content:\n" + content + "\n\n" );
            }
        }
        catch ( AptRepoException e )
        {
            System.out.println( e );
        }
        return null;
    }
}
