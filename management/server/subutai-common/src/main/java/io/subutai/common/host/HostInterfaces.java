package io.subutai.common.host;


import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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
    private Set<HostInterface> interfaces = new HashSet<>();


    public void addInterface( HostInterface hostInterface )
    {
        this.interfaces.add( hostInterface );
    }


    public HostInterface findByIp( final String ip )
    {
        Preconditions.checkNotNull( ip );
        HostInterface result = null;

        for ( Iterator<HostInterface> i = interfaces.iterator(); i.hasNext() && result == null; )
        {
            HostInterface c = i.next();
            if ( ip.equals( c.getIp() ) )
            {
                result = c;
            }
        }
        return result;
    }


    public HostInterface findByName( final String name )
    {
        Preconditions.checkNotNull( name );
        HostInterface result = null;

        for ( Iterator<HostInterface> i = interfaces.iterator(); i.hasNext() && result == null; )
        {
            HostInterface c = i.next();
            if ( name.equals( c.getIp() ) )
            {
                result = c;
            }
        }
        return result;
    }


    public Set<HostInterface> filterByIp( final String pattern )
    {
        Preconditions.checkNotNull( pattern );
        Set<HostInterface> result = new HashSet<>();

        result = Sets.filter( interfaces, new Predicate<Interface>()
        {
            @Override
            public boolean apply( final Interface intf )
            {
                return intf.getIp().matches( pattern );
            }
        } );

        return Collections.unmodifiableSet( result );
    }


    public Set<HostInterface> filterByName( final String pattern )
    {
        Preconditions.checkNotNull( pattern );
        Set<HostInterface> result = new HashSet<>();
        result = Sets.filter( interfaces, new Predicate<HostInterface>()
        {
            @Override
            public boolean apply( final HostInterface intf )
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
}
