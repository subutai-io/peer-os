package org.safehaus.subutai.plugin.hive.impl.handler;


import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.hive.api.HiveConfig;
import org.safehaus.subutai.plugin.hive.impl.Commands;


public class CheckInstallHandler
{

    private final ContainerHost containerHost;


    public CheckInstallHandler( ContainerHost containerHost )
    {
        this.containerHost = containerHost;
    }


    /**
     * Checks whether specified nodes have installed product.
     *
     * @return map where key is a node instance and value is boolean value indicating if the product is installed or not
     */
    public boolean check()
    {
        CommandResult result;
        try
        {
            result = containerHost.execute( new RequestBuilder( Commands.checkIfInstalled ) );
            if ( result.hasSucceeded() )
            {
                return result.getStdOut().contains( HiveConfig.PRODUCT_KEY.toLowerCase() );
            }
        }
        catch ( CommandException e )
        {
            e.printStackTrace();
        }
        return false;
    }


    public ContainerHost getContainerHost()
    {
        return containerHost;
    }
}
