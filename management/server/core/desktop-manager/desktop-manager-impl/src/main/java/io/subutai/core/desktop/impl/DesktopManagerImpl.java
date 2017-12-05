package io.subutai.core.desktop.impl;


import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.peer.ContainerHost;
import io.subutai.core.desktop.api.DesktopManager;


public class DesktopManagerImpl implements DesktopManager
{

    public DesktopManagerImpl()
    {
    }


    public void init()
    {
    }


    public void dispose()
    {
    }


    @Override
    public boolean isDesktop( final ContainerHost containerHost ) throws CommandException
    {
        String deskEnvResult = getDesktopEnvironmentInfo( containerHost );
        String rdSerResult = getRDServerInfo( containerHost );

        if ( deskEnvResult != null && rdSerResult != null )
        {
            if ( !deskEnvResult.isEmpty() && !rdSerResult.isEmpty() )
            {
                return true;
            }
        }

        return false;
    }


    @Override
    public String getDesktopEnvironmentInfo( final ContainerHost containerHost ) throws CommandException
    {
        CommandResult result = containerHost.execute( Commands.getDeskEnvSpecifyCommand() );
        return result.getStdOut();
    }


    @Override
    public String getRDServerInfo( final ContainerHost containerHost ) throws CommandException
    {
        CommandResult result = containerHost.execute( Commands.getRDServerSpecifyCommand() );
        String stdOut = result.getStdOut();
        if (stdOut!=null && !stdOut.isEmpty())
        {
            stdOut = stdOut.replace( " [ + ]  ", "" );
            stdOut = stdOut.replace( "\n", "" );
        }
        return stdOut;
    }
}
