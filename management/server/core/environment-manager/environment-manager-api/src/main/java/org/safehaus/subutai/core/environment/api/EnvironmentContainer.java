package org.safehaus.subutai.core.environment.api;


import org.safehaus.subutai.common.exception.ContainerException;
import org.safehaus.subutai.common.protocol.Container;
import org.safehaus.subutai.common.protocol.DefaultCommandMessage;
import org.safehaus.subutai.common.protocol.PeerCommandType;
import org.safehaus.subutai.core.environment.api.helper.Environment;


/**
 * Created by timur on 9/22/14.
 */
public class EnvironmentContainer extends Container
{

    private transient Environment environment;


    public Environment getEnvironment()
    {
        return environment;
    }


    public void setEnvironment( final Environment environment )
    {
        this.environment = environment;
    }


    @Override
    public boolean start() throws ContainerException
    {
        DefaultCommandMessage cmd =
                new DefaultCommandMessage( PeerCommandType.START, getEnvironment().getUuid(), getPeerId(),
                        getAgentId() );
        environment.invoke( cmd );
        return cmd.isSuccess();
    }


    @Override
    public boolean stop() throws ContainerException
    {
        DefaultCommandMessage cmd =
                new DefaultCommandMessage( PeerCommandType.STOP, getEnvironment().getUuid(), getPeerId(),
                        getAgentId() );
        environment.invoke( cmd );
        return cmd.isSuccess();
    }


    @Override
    public boolean isConnected() throws ContainerException
    {
        DefaultCommandMessage cmd =
                new DefaultCommandMessage( PeerCommandType.IS_CONNECTED, getEnvironment().getUuid(), getPeerId(),
                        getAgentId() );
        environment.invoke( cmd );
        return cmd.isSuccess();
    }
}
