package io.subutai.bazaar.share.dto.domain;


import java.util.ArrayList;
import java.util.List;


public class ContainerPortMapDto
{
    private String environmentSSId;

    private List<PortMapDto> containerPorts = new ArrayList();


    public String getEnvironmentSSId()
    {
        return environmentSSId;
    }


    public void setEnvironmentSSId( String environmentSSId )
    {
        this.environmentSSId = environmentSSId;
    }


    public List<PortMapDto> getContainerPorts()
    {
        return containerPorts;
    }


    public void setContainerPorts( List<PortMapDto> containerPorts )
    {
        this.containerPorts = containerPorts;
    }
}
