package io.subutai.common.host;


import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;


/**
 * Host interfaces collection
 */
public class HostInterfaces
{
    private static final Logger LOG = LoggerFactory.getLogger( HostInterfaces.class );

    @JsonProperty
    private Set<HostInterfaceModel> interfaces = new HashSet<>();


    public HostInterfaces()
    {
    }


    public HostInterfaces( final Set<HostInterfaceModel> interfaces )
    {
        this.interfaces = interfaces;
    }


    public HostInterfaceModel findByIp( final String ip )
    {
        Preconditions.checkNotNull( ip );
        HostInterfaceModel result = NullHostInterface.getInstance();

        for ( Iterator<HostInterfaceModel> i = interfaces.iterator();
              i.hasNext() && result instanceof NullHostInterface; )
        {
            HostInterfaceModel c = i.next();
            if ( ip.equals( c.getIp() ) )
            {
                result = c;
            }
        }
        return result;
    }


    public HostInterfaceModel findByName( final String name )
    {
        Preconditions.checkNotNull( name );
        HostInterfaceModel result = NullHostInterface.getInstance();

        for ( Iterator<HostInterfaceModel> i = interfaces.iterator();
              i.hasNext() && result instanceof NullHostInterface; )
        {
            HostInterfaceModel c = i.next();
            if ( name.equals( c.getName() ) )
            {
                result = c;
            }
        }
        return result;
    }


    public Set<HostInterfaceModel> filterByIp( final String pattern )
    {
        Preconditions.checkNotNull( pattern );
        Set<HostInterfaceModel> result = new HashSet<>();

        result = Sets.filter( interfaces, new Predicate<HostInterface>()
        {
            @Override
            public boolean apply( final HostInterface intf )
            {
                return intf.getIp().matches( pattern );
            }
        } );

        return Collections.unmodifiableSet( result );
    }


    public Set<HostInterfaceModel> filterByName( final String pattern )
    {
        Preconditions.checkNotNull( pattern );
        Set<HostInterfaceModel> result = new HashSet<>();
        result = Sets.filter( interfaces, new Predicate<HostInterfaceModel>()
        {
            @Override
            public boolean apply( final HostInterfaceModel intf )
            {
                return intf.getName().matches( pattern );
            }
        } );

        return result;
    }


    public int size()
    {
        return interfaces.size();
    }


    public void addHostInterface( final HostInterfaceModel hostInterfaceModel )
    {
        this.interfaces.add( hostInterfaceModel );
    }


    @JsonIgnore
    public Set<HostInterfaceModel> getAll()
    {
        if ( this.interfaces != null )
        {
            return Collections.unmodifiableSet( this.interfaces );
        }
        else
        {
            return new HashSet<>();
        }
    }


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "HostInterfaces{" );
        sb.append( "interfaces=" ).append( interfaces );
        sb.append( '}' );
        return sb.toString();
    }
}
