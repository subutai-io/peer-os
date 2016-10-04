package io.subutai.hub.share.dto.environment.container;


import java.util.UUID;


/**
 * Command DTO obtained from Hub to execute on a container. Currently has a bare minimum of options but can be extended
 * to accommodate all command options of io.subutai.common.command.RequestBuilder
 */
public class ContainerCommandRequestDto
{
    // id of target peer
    private String peerId;

    // id of command on Hub to be able to match command with its response
    private String commandId;

    // SS container id
    private String containerId;

    // command to execute
    private String command;

    // command timeout in seconds.
    // Min value is 1, Max value is io.subutai.common.settings.Common.MAX_COMMAND_TIMEOUT_SEC
    private int timeout = 30;


    public ContainerCommandRequestDto( final String peerId, final String containerId, final String command )
    {
        this.commandId = UUID.randomUUID().toString();
        this.peerId = peerId;
        this.containerId = containerId;
        this.command = command;
    }


    public void setTimeout( final int timeout )
    {
        this.timeout = timeout;
    }


    public String getPeerId()
    {
        return peerId;
    }


    public String getCommandId()
    {
        return commandId;
    }


    public String getContainerId()
    {
        return containerId;
    }


    public String getCommand()
    {
        return command;
    }


    public int getTimeout()
    {
        return timeout;
    }
}
