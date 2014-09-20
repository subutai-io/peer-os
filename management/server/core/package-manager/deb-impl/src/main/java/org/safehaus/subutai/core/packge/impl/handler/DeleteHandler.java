package org.safehaus.subutai.core.packge.impl.handler;


import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.packge.api.PackageInfo;
import org.safehaus.subutai.core.packge.impl.DebPackageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DeleteHandler extends AbstractHandler<Boolean>
{

    public DeleteHandler( DebPackageManager pm, String hostname )
    {
        super( pm, hostname );
    }


    @Override
    Logger getLogger()
    {
        return LoggerFactory.getLogger( DeleteHandler.class );
    }


    @Override
    public Boolean performAction()
    {
        FindHandler h = new FindHandler( packageManager, hostname );
        Collection<PackageInfo> col = h.performAction();
        if ( col == null )
        {
            return Boolean.FALSE;
        }

        Agent a = getAgent();
        if ( a == null )
        {
            return false;
        }

        RequestBuilder rb =
                new RequestBuilder( "rm " + packageManager.getFilename() ).withCwd( packageManager.getLocation() );
        Command cmd = packageManager.getCommandRunner().createCommand( rb, new HashSet<>( Arrays.asList( a ) ) );
        packageManager.getCommandRunner().runCommand( cmd );
        return cmd.hasSucceeded();
    }
}
