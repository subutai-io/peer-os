package io.subutai.core.hubmanager.impl.environment;


import io.subutai.common.peer.ContainerSize;


/**
 * // TODO. Move to subutai-hub-share
 * NOTE: a gateway with vlanid (e.g. gw-110) is created after cloning container.
 */
public class PeerEnvironmentDto
{
    private String envId; // Should be UUID. Otherwise reserving VNI doesn't work.

    private long vniId;

    private String p2pSubnet; // Example: "10.11.12.0"

    private String p2pIp; // Example: "10.11.12.1"

    private String p2pSharedKey;

    private String containerHostname; // Should be UUID

    private String containerName;

    private String containerIp; // Example: "192.168.35.2"

    private String templateName;

    private ContainerSize containerSize;


    // The default constructor is required for CBOR
    public PeerEnvironmentDto()
    {
    }


    public PeerEnvironmentDto( String envId, long vniId, String p2pSubnet, String p2pIp, String p2pSharedKey,
                               String containerHostname, String containerName, String containerIp,
                               String templateName, ContainerSize containerSize )
    {
        this.envId = envId;
        this.vniId = vniId;
        this.p2pSubnet = p2pSubnet;
        this.p2pIp = p2pIp;
        this.p2pSharedKey = p2pSharedKey;
        this.containerHostname = containerHostname;
        this.containerName = containerName;
        this.containerIp = containerIp;
        this.templateName = templateName;
        this.containerSize = containerSize;
    }


    public String getEnvironmentId()
    {
        return envId;
    }


    public long getVniId()
    {
        return vniId;
    }


    public String getP2pSubnet()
    {
        return p2pSubnet;
    }


    public String getP2pIp()
    {
        return p2pIp;
    }


    public String getP2pSharedKey()
    {
        return p2pSharedKey;
    }


    public String getContainerHostname()
    {
        return containerHostname;
    }


    public String getContainerName()
    {
        return containerName;
    }


    public String getContainerIp()
    {
        return containerIp;
    }


    public String getTemplateName()
    {
        return templateName;
    }


    public ContainerSize getContainerSize()
    {
        return containerSize;
    }
}
