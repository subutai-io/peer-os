package org.safehaus.subutai.core.peer.impl;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.DefaultCommandMessage;
import org.safehaus.subutai.common.protocol.PeerCommandMessage;
import org.safehaus.subutai.common.protocol.PeerCommandType;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.command.api.command.CommandException;
import org.safehaus.subutai.core.peer.api.Host;
import org.safehaus.subutai.core.peer.api.PeerException;


/**
 * ContainerHost implementation.
 */
public class ContainerHost extends Host
{
    private String templateName;
    private UUID environmentId;


    @Override
    public String getHostname()
    {
        return getAgent() == null ? "Unknown container host" : getAgent().getHostname();
    }


    @Override
    public void invoke( final PeerCommandMessage commandMessage ) throws PeerException
    {
        getParentHost().invoke( commandMessage );
    }


    /**
     * Stops container
     *
     * @return true if all went ok, false otherwise
     */
    public boolean stop()
    {
        DefaultCommandMessage stopCommand =
                new DefaultCommandMessage( PeerCommandType.STOP, null, null, getAgent().getUuid() );
        try
        {
            invoke( stopCommand );
            return stopCommand.isSuccess();
        }
        catch ( PeerException e )
        {
            return false;
        }
    }


    /**
     * Starts container
     *
     * @return true if all went ok, false otherwise
     */
    public boolean start() throws CommandException
    {
        DefaultCommandMessage startCommand =
                new DefaultCommandMessage( PeerCommandType.START, null, null, getAgent().getUuid() );
        try
        {
            invoke( startCommand );
            return startCommand.isSuccess();
        }
        catch ( PeerException e )
        {
            return false;
        }
    }


    public Template getTemplate()
    {
        DefaultCommandMessage commandMessage =
                new DefaultCommandMessage( PeerCommandType.GET_TEMPLATE, null, null, getAgent().getUuid() );
        commandMessage.setInput( templateName );
        try
        {
            invoke( commandMessage );
            if ( commandMessage.isSuccess() )
            {
                Template template = JsonUtil.fromJson( commandMessage.getResult().toString(), Template.class );
                return template;
            }
        }
        catch ( PeerException e )
        {
        }
        return null;
    }


    public String getTemplateName()
    {
        return templateName;
    }


    public void setTemplateName( final String templateName )
    {
        this.templateName = templateName;
    }


    public UUID getEnvironmentId()
    {
        return environmentId;
    }


    public void setEnvironmentId( final UUID environmentId )
    {
        this.environmentId = environmentId;
    }
}
