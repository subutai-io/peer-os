package io.subutai.core.karaf.manager.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.karaf.manager.api.KarafManager;


/**
 *
 */
@Command( scope = "karaf", name = "command", description = "execute karaf shell command" )
public class KarafShellCommand extends SubutaiShellCommandSupport
{
    private KarafManager karafManager = null;

    @Argument( index = 0, name = "shell command", required = true, multiValued = false,
            description = "Shell Command" )
    String shellCommand;

    public KarafShellCommand(KarafManager karafManager)
    {
        this.karafManager = karafManager;
    }


    @Override
    protected Object doExecute()
    {

        try
        {
            String result = karafManager.executeShellCommand( shellCommand );

            System.out.println( String.format( "Command Started:%s", shellCommand ) );
            System.out.println( String.format( "Result:%s", result) );
        }
        catch ( Exception e )
        {
            System.out.println( e.getMessage() );
        }

        return null;
    }
}
