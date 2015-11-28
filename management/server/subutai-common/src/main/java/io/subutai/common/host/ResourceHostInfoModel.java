package io.subutai.common.host;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Sets;

import io.subutai.common.util.CollectionUtil;
import io.subutai.common.util.JsonUtil;


/**
 * Implementation of ResourceHostInfo
 */
public class ResourceHostInfoModel extends HostInfoModel implements ResourceHostInfo
{
    private Set<ContainerHostInfoModel> containers;
    @JsonIgnore
    private InstanceType instance;
    @JsonIgnore
    private Set<Alert> alert;


    public ResourceHostInfoModel( final HostInfo hostInfo )
    {
        super( hostInfo );
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
                          .add( "instance", instance ).add( "interfaces", getHostInterfaces() ).add( "containers", containers )
                          .toString();
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


    /*
            {
                "alert": [
                    {
                        "id": "CE399A38D78A8815C95A87A5B54FA60751092C73",
                        "cpu": {
                            "current": 87,
                            "quota": 15
                        },
                        "ram": {
                            "current": 90,
                            "quota": 1024
                        },
                        "hdd": [
                            {
                                "partition": "Var",
                                "current": 86,
                                "quota": 10
                            },
                            {
                                "partition": "Opt",
                                "current": 86,
                                "quota": 10
                            }
                        ]
                    }
                ]
            }
    */
    public class Alert
    {
        String id;
        Cpu cpu;
        Ram ram;
        Set<Hdd> hdd;


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
    }


    public class Cpu
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


    public class Ram
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
    }


    public class Hdd
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
    }

//
//    public static void main( String[] args )
//    {
//        ResourceHostInfoModel info = new ResourceHostInfoModel();
//        info.test();
//    }


    private void test()
    {
        Alert alert = new Alert();
        alert.id = UUID.randomUUID().toString();
        //        alert.cpu = new Cpu( 1, 2 );
        alert.ram = new Ram( "3", "4" );

        Hdd opt = new Hdd( "Opt", "5", "6" );
        Hdd home = new Hdd( "Home", "5", "6" );
        Hdd var = new Hdd( "Var", "5", "6" );
        alert.hdd = new HashSet<>();
        alert.hdd.add( opt );
        alert.hdd.add( home );
        alert.hdd.add( var );
        final String json = JsonUtil.toJson( alert );
        System.out.println( json );

        Alert a = JsonUtil.fromJson( json, Alert.class );
        final String aJson = JsonUtil.toJson( a );
        System.out.println( aJson );
        System.out.println( json.equals( aJson ) );
        System.out.println( a.cpu );
    }
}
