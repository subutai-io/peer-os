package io.subutai.core.pluginmanager.impl;


import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.command.RequestBuilder;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.pluginmanager.api.OperationType;
import io.subutai.core.pluginmanager.api.PluginManagerException;


public class PluginOperationHandler implements Runnable
{
    private static final Logger LOGGER = LoggerFactory.getLogger( PluginOperationHandler.class );
    private PluginManagerImpl manager;
    private ManagerHelper managerHelper;
    private String pluginName;
    private OperationType operationType;
    private TrackerOperation trackerOperation;
    private static boolean isInstallSuccessful = false;
    private static boolean isRemoveSuccessful = false;


    public PluginOperationHandler( PluginManagerImpl manager, ManagerHelper managerHelper, String pluginName,
                                   OperationType operationType )
    {
        this.manager = manager;
        this.managerHelper = managerHelper;
        this.pluginName = pluginName;
        this.operationType = operationType;
        this.trackerOperation = manager.getTracker().createTrackerOperation( manager.getProductKey(),
                String.format( "Creating %s tracker object...", pluginName ) );
    }


    public UUID getTrackerId()
    {
        return trackerOperation.getId();
    }


    @Override
    public void run()
    {
        switch ( operationType )
        {
            case INSTALL:
                installPlugin();
                break;
            case REMOVE:
                removePlugin();
                break;
            case UPGRADE:
                upgradePlugin();
                break;
            default:
                break;
        }
    }


    public void removePlugin()
    {
        trackerOperation.addLog( "Removing plugin.." );

        try
        {
            RequestBuilder command = manager.getCommands().makeRemoveCommand( pluginName );
            if ( managerHelper.execute( command ) == null )
            {
                throw new PluginManagerException( "Remove operation is failed." );
            }
        }
        catch ( PluginManagerException e )
        {
            LOGGER.warn( "Warning remove operation failed", e );
            setIsRemoveSuccessful( false );
            trackerOperation.addLogFailed( String.format( "%s, Removing operation is failed...", e.getMessage() ) );
            return;
        }
        setIsRemoveSuccessful( true );
        trackerOperation.addLogDone( "Plugin is removed successfully." );
    }


    private void installPlugin()
    {
        trackerOperation.addLog( "Installing plugin.." );
        try
        {
            RequestBuilder command = manager.getCommands().makeInstallCommand( pluginName );
            if ( managerHelper.execute( command ) == null )
            {
                throw new PluginManagerException( "Installation is failed" );
            }
            if ( !manager.isInstalled( pluginName ) )
            {
                trackerOperation.addLogFailed( "Installation failed" );
                return;
            }
        }
        catch ( PluginManagerException e )
        {
            LOGGER.warn( "Warning failed in install operation", e );
            setIsInstallSuccessful( false );
            trackerOperation.addLogFailed( String.format( "%s, Installing operation is failed...", e.getMessage() ) );
            return;
        }
        setIsInstallSuccessful( true );
        trackerOperation.addLogDone( "Plugin is installed successfully." );
    }


    private void upgradePlugin()
    {
        trackerOperation.addLog( "Upgrading plugin.." );
        RequestBuilder command = manager.getCommands().makeUpgradeCommand( pluginName );
        try
        {
            managerHelper.execute( command );
        }
        catch ( PluginManagerException e )
        {
            LOGGER.warn( "Warning upgrade operation failed", e );
            trackerOperation.addLogFailed( String.format( "%s, Upgrade operation is failed...", e.getMessage() ) );
        }
        trackerOperation.addLogDone( "Plugin is upgraded successfully." );
    }


    private static synchronized void setIsInstallSuccessful( final boolean state )
    {
        isInstallSuccessful = state;
    }


    private static synchronized void setIsRemoveSuccessful( final boolean state )
    {
        isRemoveSuccessful = state;
    }


    public static boolean isRemoveSuccessful()
    {
        return isRemoveSuccessful;
    }


    public static boolean isInstallSuccessful()
    {
        return isInstallSuccessful;
    }
}
