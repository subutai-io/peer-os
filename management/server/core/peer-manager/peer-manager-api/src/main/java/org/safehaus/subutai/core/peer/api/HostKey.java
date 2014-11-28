package org.safehaus.subutai.core.peer.api;


/**
 * Created by timur on 11/27/14.
 */
public class HostKey
{
    private String peerId;
    private String creatorId;
    private String hostId;
    private String hostname;
    private String environmentId;
    private String nodeGroupName;


    public HostKey( final String hostId, final String peerId, final String creatorId, final String hostname,
                    final String environmentId, final String nodeGroupName )
    {
        this.peerId = peerId;
        this.creatorId = creatorId;
        this.hostId = hostId;
        this.hostname = hostname;
        this.environmentId = environmentId;
        this.nodeGroupName = nodeGroupName;
    }


    public String getHostId()
    {
        return hostId;
    }


    public String getHostname()
    {
        return hostname;
    }


    public String getPeerId()
    {
        return peerId;
    }


    public String getEnvironmentId()
    {
        return environmentId;
    }


    public String getNodeGroupName()
    {
        return nodeGroupName;
    }


    public String getCreatorId()
    {
        return creatorId;
    }
}
