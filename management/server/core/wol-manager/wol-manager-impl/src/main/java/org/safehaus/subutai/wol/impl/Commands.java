package org.safehaus.subutai.wol.impl;

import com.google.common.collect.Lists;
import org.safehaus.subutai.common.command.RequestBuilder;

/**
 * Created by emin on 11/17/14.
 */

public class Commands
{
    public RequestBuilder getSendWakeOnLanCommand( String macID )
    {
        return new RequestBuilder("sudo wakeonlan -i 10.10.10.255").
                withCmdArgs(Lists.newArrayList( macID ) );
    }
}
