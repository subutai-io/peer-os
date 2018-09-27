package io.subutai.core.bazaarmanager.impl.util;


import io.subutai.common.peer.ContainerHost;
import io.subutai.common.util.TaskUtil;


public class Utils
{
    private Utils()
    {
    }


    public static boolean waitTillConnects( ContainerHost containerHost, int maxTimeoutSec )
    {
        int waited = 0;
        while ( !containerHost.isConnected() && waited < maxTimeoutSec )
        {
            waited++;
            TaskUtil.sleep( 1000 );
        }

        return containerHost.isConnected();
    }
}
