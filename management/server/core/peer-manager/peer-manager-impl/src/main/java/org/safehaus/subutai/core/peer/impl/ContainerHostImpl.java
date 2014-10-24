package org.safehaus.subutai.core.peer.impl;


import java.util.UUID;

import org.safehaus.subutai.core.peer.api.ContainerHost;


/**
 * ContainerHost implementation.
 */
public class ContainerHostImpl extends HostImpl implements ContainerHost
{

    private String templateName;
    private String templateArch;
    private UUID ownerPeerId;
    //
    //    @Override
    //    public String getHostname()
    //    {
    //        return getAgent() == null ? "Unknown container host" : getAgent().getHostname();
    //    }
    //
    //
    //    @Override
    //    public void invoke( final PeerCommandMessage commandMessage ) throws PeerException
    //    {
    //        getParentHost().invoke( commandMessage );
    //    }

    //
    //    /**
    //     * Stops container
    //     *
    //     * @return true if all went ok, false otherwise
    //     */
    //    public boolean stop()
    //    {
    //        DefaultCommandMessage stopCommand =
    //                new DefaultCommandMessage( PeerCommandType.STOP, null, getAgent().getUuid() );
    //        try
    //        {
    //            invoke( stopCommand );
    //            return stopCommand.isSuccess();
    //        }
    //        catch ( PeerException e )
    //        {
    //            return false;
    //        }
    //    }
    //
    //
    //    /**
    //     * Starts container
    //     *
    //     * @return true if all went ok, false otherwise
    //     */
    //    public boolean start() throws CommandException
    //    {
    //        DefaultCommandMessage startCommand =
    //                new DefaultCommandMessage( PeerCommandType.START, null, getAgent().getUuid() );
    //        try
    //        {
    //            invoke( startCommand );
    //            return startCommand.isSuccess();
    //        }
    //        catch ( PeerException e )
    //        {
    //            return false;
    //        }
    //    }
    //
    //
    //    public Template getTemplate()
    //    {
    //        DefaultCommandMessage commandMessage =
    //                new DefaultCommandMessage( PeerCommandType.GET_TEMPLATE, null, getAgent().getUuid() );
    //        commandMessage.setInput( templateName );
    //        try
    //        {
    //            invoke( commandMessage );
    //            if ( commandMessage.isSuccess() )
    //            {
    //                Template template = JsonUtil.fromJson( commandMessage.getResult().toString(), Template.class );
    //                return template;
    //            }
    //        }
    //        catch ( PeerException e )
    //        {
    //        }
    //        return null;
    //    }


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
        return getAgent().getEnvironmentId();
    }


    @Override
    public String getTemplateArch()
    {
        return templateArch;
    }


    @Override
    public UUID getOwnerPeerId()
    {
        return ownerPeerId;
    }


    public void setOwnerPeerId( final UUID ownerPeerId )
    {
        this.ownerPeerId = ownerPeerId;
    }
}
