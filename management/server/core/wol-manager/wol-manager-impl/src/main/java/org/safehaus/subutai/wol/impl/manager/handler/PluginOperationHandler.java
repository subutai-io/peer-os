package org.safehaus.subutai.wol.impl.manager.handler;


import java.util.UUID;

import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.wol.api.PluginInfo;
import org.safehaus.subutai.wol.api.PluginManager;
import org.safehaus.subutai.wol.api.PluginManagerException;
import org.safehaus.subutai.wol.impl.manager.Commands;
import org.safehaus.subutai.wol.impl.manager.ManagerHelper;
import org.safehaus.subutai.wol.impl.manager.PluginInfoImpl;
import org.safehaus.subutai.wol.impl.manager.PluginManagerImpl;


/**
 * Created by ebru on 15.12.2014.
 */
public class PluginOperationHandler implements Runnable
{
    private PluginManagerImpl manager;
    private ManagerHelper managerHelper;
    private String pluginName;
    private OperationType operationType;
    private TrackerOperation trackerOperation;

    public PluginOperationHandler( PluginManagerImpl manager, ManagerHelper managerHelper, String pluginName, OperationType operationType)
    {
        this.manager = manager;
        this.managerHelper = managerHelper;
        this.pluginName = pluginName;
        this.operationType = operationType;
        this.trackerOperation = manager.getTracker().createTrackerOperation( manager.getProductKey(), String.format( "Creating %s tracker object...", pluginName )  );
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
        RequestBuilder command = manager.getCommands().makeRemoveCommand( pluginName );
        try
        {
            managerHelper.execute( command );
        }
        catch ( PluginManagerException e )
        {
            trackerOperation.addLogFailed( String.format( "%s, Removing operation is failed...", e.getMessage() ) );
        }
        trackerOperation.addLogDone( "Plugin is removed successfully." );
    }


    private void installPlugin()
    {
        trackerOperation.addLog( "Installing plugin.." );
        RequestBuilder command = manager.getCommands().makeInstallCommand( pluginName );
        try
        {
            managerHelper.execute( command );
        }
        catch ( PluginManagerException e )
        {
            trackerOperation.addLogFailed( String.format( "%s, Installing operation is failed...", e.getMessage() ) );
        }
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
