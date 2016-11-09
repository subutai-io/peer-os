package io.subutai.hub.share.dto.host;


import java.util.UUID;


/**
 * Command DTO obtained from Hub to execute on a host. Currently has a bare minimum of options but can be extended
 * to accommodate all command options of io.subutai.common.command.RequestBuilder
 */
public class CommandRequestDto
{
    // id of target peer
    private String peerId;

    // id of command on Hub to be able to match command with its response
    private String commandId;

    // SS host id
    private String hostId;

    // command to execute
    private String command;

    // command timeout in seconds.
    // Min value is 1, Max value is io.subutai.common.settings.Common.MAX_COMMAND_TIMEOUT_SEC
    private int timeout = 30;

    private boolean grabOutput = true;


    protected CommandRequestDto()
    {
    }


    public CommandRequestDto( final String peerId, final String hostId, final String command )
    {
        this.commandId = UUID.randomUUID().toString();
        this.peerId = peerId;
        this.hostId = hostId;
        this.command = command;
    }


    public CommandRequestDto setTimeout( final int timeout )
    {
        this.timeout = timeout;

        return this;
    }


    public CommandRequestDto setGrabOutput( final boolean grabOutput )
    {
        this.grabOutput = grabOutput;

        return this;
    }


    public boolean grabOutput()
    {
        return grabOutput;
    }


    public String getPeerId()
    {
        return peerId;
    }


    public String getCommandId()
    {
        return commandId;
    }


    public String getHostId()
    {
        return hostId;
    }


    public String getCommand()
    {
        return command;
    }


    public int getTimeout()
    {
        return timeout;
    }


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "CommandRequestDto{" );
        sb.append( "peerId='" ).append( peerId ).append( '\'' );
        sb.append( ", commandId='" ).append( commandId ).append( '\'' );
        sb.append( ", hostId='" ).append( hostId ).append( '\'' );
        sb.append( ", command='" ).append( command ).append( '\'' );
        sb.append( ", timeout=" ).append( timeout );
        sb.append( ", grabOutput=" ).append( grabOutput );
        sb.append( '}' );
        return sb.toString();
    }
}
