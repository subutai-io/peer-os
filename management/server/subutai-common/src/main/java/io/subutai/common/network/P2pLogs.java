package io.subutai.common.network;


import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;


public class P2pLogs
{
    Set<String> logs = Sets.newHashSet();


    public void addLog( String log )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( log ) );

        logs.add( log );
    }


    public Set<String> getLogs()
    {
        return logs;
    }


    public boolean isEmpty()
    {
        return logs.isEmpty();
    }
}
