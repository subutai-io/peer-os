package org.safehaus.subutai.core.environment.api;


import java.util.UUID;

import org.safehaus.subutai.common.exception.ContainerException;
import org.safehaus.subutai.common.protocol.Container;
import org.safehaus.subutai.common.protocol.DefaultCommandMessage;
import org.safehaus.subutai.common.protocol.PeerCommandType;


/**
 * Created by timur on 9/22/14.
 */
public class EnvironmentContainer extends Container
{

    private UUID environmentId;
    private String templateName;


    public String getTemplateName()
    {
        return templateName;
    }


    public void setTemplateName( final String templateName )
    {
        this.templateName = templateName;
    }

    //    private transient Environment environment;


    /*@Override
    public UUID getEnvironmentId()
    {
        return environment.getUuid();
    }*/


    @Override
    public UUID getEnvironmentId()
    {
        return environmentId;
    }


    public void setEnvironmentId( final UUID environmentId )
    {
        this.environmentId = environmentId;
    }


    @Override
    public DefaultCommandMessage start() throws ContainerException
    {
        DefaultCommandMessage cmd =
                new DefaultCommandMessage( PeerCommandType.START, environmentId, getPeerId(), getAgentId() );
        //        environment.invoke( cmd );
        //        return cmd.isSuccess();
        return cmd;
    }


    /*public Environment getEnvironment()
    {
        return environment;
    }


    public void setEnvironment( final Environment environment )
    {
        this.environment = environment;
    }*/


    @Override
    public DefaultCommandMessage stop() throws ContainerException
    {
        DefaultCommandMessage cmd =
                new DefaultCommandMessage( PeerCommandType.STOP, environmentId, getPeerId(), getAgentId() );
        //        environment.invoke( cmd );
        //        return cmd.isSuccess();
        return cmd;
    }


    @Override
    public DefaultCommandMessage isConnected() throws ContainerException
    {
        DefaultCommandMessage cmd =
                new DefaultCommandMessage( PeerCommandType.IS_CONNECTED, environmentId, getPeerId(), getAgentId() );
        //        environment.invoke( cmd );
        //        return cmd.isSuccess();
        return cmd;
    }
}
