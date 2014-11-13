package org.safehaus.subutai.common.protocol;


import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * Used to define a physical/lxc host on the network. It could be management server, physical server or container. It
 * just defines a host on the network.
 */
@Entity(name = "Agent")
@NamedQueries({
        @NamedQuery(name = "Agent.getAll", query = "SELECT a FROM Agent a")
})
@XmlRootElement(name = "")
public class Agent implements Serializable, Comparable<Agent>
{

    public static final String QUERY_GET_ALL = "Agent.getAll";

    private UUID uuid;
    private String macAddress;
    private String hostname;

    @ElementCollection(targetClass = String.class)
    private List<String> listIP;

    private boolean isLXC;
    private String parentHostName;
    private String transportId;
//    private UUID siteId;
//    private UUID environmentId;


    public Agent( UUID uuid, String hostname, String parentHostName, String macAddress, List<String> listIP,
                  boolean isLXC, String transportId )
    {
        Preconditions.checkNotNull( uuid, "UUID is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ), "Hostname is null or empty" );
        //        Preconditions.checkNotNull( siteId, "Site id is null" );
        //        Preconditions.checkNotNull( environmentId, "Environment id is null" );

        this.uuid = uuid;
        this.macAddress = macAddress;
        this.hostname = hostname;
        this.listIP = listIP;
        this.isLXC = isLXC;
        this.parentHostName = parentHostName;
        this.transportId = transportId;
        //        this.siteId = siteId;
        //        this.environmentId = environmentId;
    }


//    public UUID getSiteId()
    //    {
    //        return siteId;
    //    }
    //
    //
    //    public UUID getEnvironmentId()
    //    {
    //        return environmentId;
    //    }


    public String getTransportId()
    {
        return transportId;
    }


    public String getParentHostName()
    {
        return parentHostName;
    }


    public boolean isLXC()
    {
        return isLXC;
    }


    public UUID getUuid()
    {
        return uuid;
    }


    public String getMacAddress()
    {
        return macAddress;
    }


    public List<String> getListIP()
    {
        return Collections.unmodifiableList( listIP );
    }


    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 83 * hash + ( this.uuid != null ? this.uuid.hashCode() : 0 );
        return hash;
    }


    @Override
    public boolean equals( Object obj )
    {
        if ( obj == null )
        {
            return false;
        }
        if ( getClass() != obj.getClass() )
        {
            return false;
        }
        final Agent other = ( Agent ) obj;
        return !( this.uuid != other.uuid && ( this.uuid == null || !this.uuid.equals( other.uuid ) ) );
    }


    @Override
    public String toString()
    {
        return "Agent{" +
                "uuid=" + uuid +
                ", macAddress='" + macAddress + '\'' +
                ", hostname='" + hostname + '\'' +
                ", listIP=" + listIP +
                ", isLXC=" + isLXC +
                ", parentHostName='" + parentHostName + '\'' +
                ", transportId='" + transportId + '\'' +
//                ", siteId=" + siteId +
//                ", environmentId=" + environmentId +
                '}';
    }


    @Override
    public int compareTo( Agent o )
    {
        if ( hostname != null && o != null )
        {
            return hostname.compareTo( o.getHostname() );
        }

        return -1;
    }


    public String getHostname()
    {
        return hostname;
    }


    public void setHostname( String hostname )
    {
        this.hostname = hostname;
    }
}
