package org.safehaus.subutai.wol.impl;


import org.safehaus.subutai.common.command.RequestBuilder;

import com.google.common.collect.Lists;


public class Commands
{
    public RequestBuilder getSendWakeOnLanCommand( String macID )
    {
        return new RequestBuilder( "sudo wakeonlan -i 10.10.10.255" ).
                                                                             withCmdArgs( Lists.newArrayList( macID ) );
    }
}
