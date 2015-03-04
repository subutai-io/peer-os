package org.safehaus.subutai.pluginmanager.impl;


import java.util.UUID;

import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.pluginmanager.api.OperationType;
import org.safehaus.subutai.pluginmanager.api.PluginManagerException;


public class PluginOperationHandler implements Runnable
{
    private PluginManagerImpl manager;
    private ManagerHelper managerHelper;
    private String pluginName;
    private OperationType operationType;
    private TrackerOperation trackerOperation;
    public static boolean isInstallSuccessful = false;
    public static boolean isRemoveSuccessful = false;


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
        }
    }


    public void removePlugin()
    {
        trackerOperation.addLog( "Removing plugin.." );

        try
        {
            RequestBuilder command = manager.getCommands().makeRemoveCommand( pluginName );
            if( managerHelper.execute( command ) == null )
            {
                throw new PluginManagerException( "Remove operation is failed." );
            }
        }
        catch ( PluginManagerException e )
        {
            isRemoveSuccessful = false;
            trackerOperation.addLogFailed( String.format( "%s, Removing operation is failed...", e.getMessage() ) );
            return;
        }
        isRemoveSuccessful = true;
        trackerOperation.addLogDone( "Plugin is removed successfully." );

    }


    private void installPlugin()
    {
        trackerOperation.addLog( "Installing plugin.." );
        try
        {
            RequestBuilder command = manager.getCommands().makeInstallCommand( pluginName );
            if( managerHelper.execute( command ) == null )
            {
                throw  new PluginManagerException( "Installation is failed" );
            }
            if( !manager.isInstalled( pluginName ))
            {
                trackerOperation.addLogFailed( "Installation failed" );
                return;
            }
        }
        catch ( PluginManagerException e )
        {
            isInstallSuccessful = false;
            trackerOperation.addLogFailed( String.format( "%s, Installing operation is failed...", e.getMessage() ) );
            return;
        }
        isInstallSuccessful = true;
        trackerOperation.addLogDone( "Plugin is installed successfully." );

    }


    private void upgradePlugin()
    {
        String result = null;
        trackerOperation.addLog( "Upgrading plugin.." );
        RequestBuilder command = manager.getCommands().makeUpgradeCommand( pluginName );
        try
        {
            result = managerHelper.execute( command );
            /*if( result.equals( "fail" ))
            {
                throw new PluginManagerException( "Operation is failed" );
            }*/
        }
        catch ( PluginManagerException e )
        {
            trackerOperation.addLogFailed( String.format( "%s, Upgrade operation is failed...", e.getMessage() ) );
        }
        trackerOperation.addLogDone( "Plugin is upgraded successfully." );
    }
}
