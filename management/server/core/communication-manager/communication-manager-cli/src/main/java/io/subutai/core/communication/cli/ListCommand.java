package io.subutai.core.communication.cli;


import java.util.List;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import io.subutai.core.communication.api.CommunicationManager;


@Command( scope = "cm", name = "list", description = "List available recipients" )
public class ListCommand extends OsgiCommandSupport
{

    private CommunicationManager communicationManager;


    public ListCommand( final CommunicationManager communicationManager )
    {
        this.communicationManager = communicationManager;
    }

    @Override
    protected Object doExecute() throws Exception
    {
        System.out.println("Avilable recipients:");
        List<String> list = communicationManager.getRecipients();
        System.out.println( "Found " + list.size() + " recipients" );
        for ( String url : list )
        {
            System.out.println( url );
        }
        return null;
    }
}
