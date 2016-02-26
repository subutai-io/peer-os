package io.subutai.common.environment;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.subutai.common.host.HostInfo;
import io.subutai.common.tracker.OperationMessage;


public class CreateEnvironmentContainerGroupResponse
{
    private Set<HostInfo> hosts = new HashSet<>();
    private List<OperationMessage> messages = new ArrayList<>();


    public CreateEnvironmentContainerGroupResponse()
    {
    }


    public List<OperationMessage> getMessages()
    {
        return messages;
    }


    public Set<HostInfo> getHosts()
    {
        return hosts;
    }


    public void addHostInfo( final HostInfo hostInfo )
    {
        if ( hostInfo == null )
        {
            throw new IllegalArgumentException( "ContainerHostInfoModel could not be null." );
        }

        this.hosts.add( hostInfo );
    }


    public void addFailMessage( final String msg )
    {
        if ( msg == null )
        {
            throw new IllegalArgumentException( "Fail message could not be null." );
        }

        this.messages.add( new OperationMessage( msg, OperationMessage.Type.FAILED ) );
    }


    public void addSucceededMessages( final String msg )
    {
        if ( msg == null )
        {
            throw new IllegalArgumentException( "Message could not be null." );
        }

        this.messages.add( new OperationMessage( msg, OperationMessage.Type.SUCCEEDED ) );
    }
}
