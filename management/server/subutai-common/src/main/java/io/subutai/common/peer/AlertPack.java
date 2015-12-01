package io.subutai.common.peer;


import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import io.subutai.common.metric.AlertResource;


/**
 * Alert packet for transferring between peers
 */
public class AlertPack
{
    @JsonProperty( "peerId" )
    String peerId;
    @JsonProperty( "environmentId" )
    String environmentId;
    @JsonProperty( "containerId" )
    String containerId;
    @JsonProperty( "templateName" )
    String templateName;
    @JsonProperty( "resource" )
    AlertResource resource;
    @JsonIgnore
    boolean delivered = false;


    public AlertPack( @JsonProperty( "peerId" ) final String peerId,
                      @JsonProperty( "environmentId" ) final String environmentId,
                      @JsonProperty( "containerId" ) final String containerId,
                      @JsonProperty( "templateName" ) final String templateName,
                      @JsonProperty( "resource" ) final AlertResource resource )
    {
        this.peerId = peerId;
        this.environmentId = environmentId;
        this.containerId = containerId;
        this.resource = resource;
        this.templateName = templateName;
    }


    public String getPeerId()
    {
        return peerId;
    }


    public String getEnvironmentId()
    {
        return environmentId;
    }


    public String getContainerId()
    {
        return containerId;
    }


    public AlertResource getResource()
    {
        return resource;
    }


    public String getTemplateName()
    {
        return templateName;
    }


    public boolean isDelivered()
    {
        return delivered;
    }


    public void setDelivered( final boolean delivered )
    {
        this.delivered = delivered;
    }


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "AlertPacket{" );
        sb.append( "peerId='" ).append( peerId ).append( '\'' );
        sb.append( ", environmentId='" ).append( environmentId ).append( '\'' );
        sb.append( ", containerId='" ).append( containerId ).append( '\'' );
        sb.append( ", templateName='" ).append( templateName ).append( '\'' );
        sb.append( ", value=" ).append( resource );
        sb.append( ", delivered=" ).append( delivered );
        sb.append( '}' );
        return sb.toString();
    }
}
