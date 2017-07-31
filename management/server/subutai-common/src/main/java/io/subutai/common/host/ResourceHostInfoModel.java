package io.subutai.common.host;


import java.io.Serializable;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Sets;

import io.subutai.common.util.CollectionUtil;


/**
 * Implementation of ResourceHostInfo
 */
public class ResourceHostInfoModel extends HostInfoModel implements ResourceHostInfo
{
    private Set<ContainerHostInfoModel> containers = Sets.newHashSet();
    @JsonIgnore
    private InstanceType instance;
    @JsonIgnore
    private Set<Alert> alert = Sets.newHashSet();
    @JsonIgnore
    private String address;


    public ResourceHostInfoModel( final ResourceHostInfo resourceHostInfo )
    {
        super( resourceHostInfo );

        this.instance = resourceHostInfo.getInstanceType();
        for ( ContainerHostInfo containerHostInfo : resourceHostInfo.getContainers() )
        {
            containers.add( new ContainerHostInfoModel( containerHostInfo ) );
        }
        this.address = resourceHostInfo.getAddress();
    }


    @Override
    public String getAddress()
    {
        return address;
    }


    @Override
    public String getId()
    {
        return id;
    }


    @Override
    public String getHostname()
    {
        return hostname;
    }


    @Override
    @JsonIgnore
    public InstanceType getInstanceType()
    {
        return instance;
    }


    @JsonIgnore
    public Set<Alert> getAlerts()
    {
        return alert;
    }


    @JsonIgnore
    @Override
    public Set<ContainerHostInfo> getContainers()
    {
        Set<ContainerHostInfo> result = Sets.newHashSet();

        if ( !CollectionUtil.isCollectionEmpty( containers ) )
        {
            result.addAll( containers );
        }

        return result;
    }


    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper( this ).add( "id", id ).add( "hostname", hostname )
                          .add( "instance", instance ).add( "interfaces", getHostInterfaces() )
                          .add( "containers", containers ).toString();
    }


    @Override
    public int compareTo( final HostInfo o )
    {
        if ( hostname != null && o != null )
        {
            return hostname.compareTo( o.getHostname() );
        }
        return -1;
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof ResourceHostInfoModel ) )
        {
            return false;
        }

        final ResourceHostInfoModel that = ( ResourceHostInfoModel ) o;

        if ( id != null ? !id.equals( that.id ) : that.id != null )
        {
            return false;
        }

        return true;
    }


    @Override
    public int hashCode()
    {
        return id != null ? id.hashCode() : 0;
    }


    public class Alert implements Serializable
    {
        private String id;
        private Cpu cpu;
        private Ram ram;
        private Set<Hdd> hdd;


        public String getId()
        {
            return id;
        }


        public Cpu getCpu()
        {
            return cpu;
        }


        public Ram getRam()
        {
            return ram;
        }


        public Set<Hdd> getHdd()
        {
            return hdd;
        }


        @Override
        public String toString()
        {
            return "Alert{" + "id='" + id + '\'' + ", cpu=" + cpu + ", ram=" + ram + ", hdd=" + hdd + '}';
        }
    }


    public class Cpu implements Serializable
    {
        String current;
        String quota;


        public Cpu( final String current, final String quota )
        {
            this.current = current;
            this.quota = quota;
        }


        public String getCurrent()
        {
            return current;
        }


        public String getQuota()
        {
            return quota;
        }
    }


    public class Ram implements Serializable
    {
        String current;
        String quota;


        public Ram( final String current, final String quota )
        {
            this.current = current;
            this.quota = quota;
        }


        public String getCurrent()
        {
            return current;
        }


        public String getQuota()
        {
            return quota;
        }


        @Override
        public String toString()
        {
            return "Ram{" + "current='" + current + '\'' + ", quota='" + quota + '\'' + '}';
        }
    }


    public class Hdd implements Serializable
    {
        String partition;
        String current;
        String quota;


        public Hdd( final String partition, final String current, final String quota )
        {
            this.partition = partition;
            this.current = current;
            this.quota = quota;
        }


        public String getPartition()
        {
            return partition;
        }


        public String getCurrent()
        {
            return current;
        }


        public String getQuota()
        {
            return quota;
        }


        @Override
        public String toString()
        {
            return "Hdd{" + "partition='" + partition + '\'' + ", current='" + current + '\'' + ", quota='" + quota
                    + '\'' + '}';
        }
    }
}
