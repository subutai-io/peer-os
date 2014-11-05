package org.safehaus.subutai.core.template.cli;


import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.agent.api.AgentManager;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


@Command( scope = "container", name = "attach" )
public class AttachAndExecute extends OsgiCommandSupport
{

    //    ContainerManager containerManager;
    AgentManager agentManager;

    @Argument( index = 0, required = true )
    private String hostname;
    @Argument( index = 1, required = true )
    private String cloneName;
    @Argument( index = 2, required = true )
    private String command;
    @Argument( index = 3 )
    private int timeoutInSeconds = 60;


    //    public void setContainerManager( ContainerManager containerManager )
    //    {
    //        this.containerManager = containerManager;
    //    }


    public void setAgentManager( AgentManager agentManager )
    {
        this.agentManager = agentManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {

        Agent a = agentManager.getAgentByHostname( hostname );
        /*boolean b = containerManager.attachAndExecute( a, cloneName, command, timeoutInSeconds, TimeUnit.SECONDS );
        if ( b )
        {
            System.out.println( "Command successfully executed" );
        }
        else
        {
            System.out.println( "Failed to execute command" );
        }*/
        return null;
    }
}
