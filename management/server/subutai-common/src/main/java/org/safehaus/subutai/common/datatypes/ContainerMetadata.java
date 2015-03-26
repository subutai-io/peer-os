package org.safehaus.subutai.common.datatypes;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.common.host.Interface;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.gson.annotations.Expose;


/**
 * Created by talas on 3/26/15.
 */
public class ContainerMetadata
{
    @Expose
    private String hostname;

    @Expose
    private Set<InterfaceImpl> netInterfaces = Sets.newHashSet();


    public ContainerMetadata( final String hostname, final Set<Interface> netInterfaces )
    {
        Preconditions.checkNotNull( hostname );
        Preconditions.checkNotNull( netInterfaces );

        this.hostname = hostname;
        for ( final Interface anInterface : netInterfaces )
        {
            InterfaceImpl interfaceImpl = new InterfaceImpl( anInterface );
            this.netInterfaces.add( interfaceImpl );
        }
    }


    public String getHostname()
    {
        return hostname;
    }


    public Set<Interface> getNetInterfaces()
    {
        return new HashSet<Interface>( netInterfaces );
    }


    private class InterfaceImpl implements Interface
    {
        private String interfaceName;
        private String ip;
        private String mac;


        public InterfaceImpl( Interface anInterface )
        {
            Preconditions.checkNotNull( anInterface );

            interfaceName = anInterface.getInterfaceName();
            ip = anInterface.getIp();
            mac = anInterface.getMac();
        }


        @Override
        public String getInterfaceName()
        {
            return interfaceName;
        }


        @Override
        public String getIp()
        {
            return ip;
        }


        @Override
        public String getMac()
        {
            return mac;
        }
    }
}
