package io.subutai.core.desktop.impl;


import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.peer.ContainerHost;
import io.subutai.core.desktop.api.DesktopManager;


public class DesktopManagerImpl implements DesktopManager
{
    private static final int CACHE_TTL_MIN = 60; //1 hour in minutes

    //KEY, Boolean (if it's desktop or not)
    private Cache<String, Boolean> hostDesktopCaches =
            CacheBuilder.newBuilder().maximumSize( 500 ).expireAfterWrite( CACHE_TTL_MIN, TimeUnit.MINUTES ).build();


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
        if ( stdOut != null && !stdOut.isEmpty() )
        {
            stdOut = stdOut.replace( " [ + ]  ", "" );
            stdOut = stdOut.replace( "\n", "" );
        }
        return stdOut;
    }


    @Override
    public void copyKeys( final ContainerHost containerHost ) throws CommandException
    {
        containerHost.execute( Commands.getCopyAuthKeysCommand() );
    }


    @Override
    public void createSSHDir( final ContainerHost containerHost ) throws CommandException
    {
        containerHost.execute( Commands.getCreateDefaultSSHDirectoryCommand() );
    }


    @Override
    public void createDesktopUser( final ContainerHost containerHost ) throws CommandException
    {
        containerHost.execute( Commands.getCreateDesktopUserCommand() );
        createSSHDir( containerHost );
        copyKeys( containerHost );
    }


    @Override
    public boolean existInCache( final String containerId )
    {
        return hostDesktopCaches.getIfPresent( containerId ) != null;
    }


    @Override
    public void invalidate( final String containerId )
    {
        hostDesktopCaches.invalidate( containerId );
    }


    @Override
    public void containerIsDesktop( final String containerId )
    {
        hostDesktopCaches.put( containerId, true );
    }


    @Override
    public void containerIsNotDesktop( final String containerId )
    {
        hostDesktopCaches.put( containerId, false );
    }
}
