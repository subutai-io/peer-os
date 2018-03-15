package io.subutai.core.desktop.impl;


import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainerId;
import io.subutai.core.desktop.api.DesktopManager;


public class DesktopManagerImpl implements DesktopManager
{

    private static final int CACHE_TTL_MIN = 60; //1 hour in minutes
    //Set of container host IDs
    private static Set<String> desktopContainerHosts = Sets.newConcurrentHashSet();
    private static Set<String> notDesktopContainerHosts = Sets.newConcurrentHashSet();

    //Map of container ID and it's last cache update time
    private static Map<String, Date> lastContainerUpdateTime = Maps.newConcurrentMap();

    private volatile long lastUpdateTime;


    public DesktopManagerImpl()
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
        if ( desktopContainerHosts.contains( containerId ) )
        {
            if ( isCacheExpired( containerId ) )
            {
                return false;
            }
        }

        return desktopContainerHosts.contains( containerId ) || notDesktopContainerHosts.contains( containerId );
    }


    @Override
    public void containerIsDesktop( final String containerId )
    {
        desktopContainerHosts.add( containerId );
        lastContainerUpdateTime.put( containerId, new Date() );
    }


    @Override
    public void containerIsNotDesktop( final String containerId )
    {
        notDesktopContainerHosts.add( containerId );
        lastContainerUpdateTime.put( containerId, new Date() );
    }


    @Override
    public void cleanCache( final String containerId )
    {
        desktopContainerHosts.remove( containerId );
        notDesktopContainerHosts.remove( containerId );
        lastContainerUpdateTime.remove( containerId );
    }


    @Override
    public Set<String> getDesktopContainers()
    {
        return desktopContainerHosts;
    }


    @Override
    public Set<String> getNotDesktopContainers()
    {
        return notDesktopContainerHosts;
    }


    private boolean isCacheExpired( String containerId )
    {
        Date lastUpdate = lastContainerUpdateTime.get( containerId );
        if ( lastUpdate != null )
        {
            long period = System.currentTimeMillis() - lastUpdate.getTime();

            if ( period < TimeUnit.MINUTES.toMillis( CACHE_TTL_MIN ) )
            {
                return false;
            }
        }

        cleanCache( containerId );
        return true;
    }
}
