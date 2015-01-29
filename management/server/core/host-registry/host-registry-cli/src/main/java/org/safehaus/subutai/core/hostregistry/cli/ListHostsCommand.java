package org.safehaus.subutai.core.hostregistry.cli;


import java.util.Set;

import javax.security.auth.login.LoginContext;

import org.safehaus.subutai.core.hostregistry.api.ContainerHostInfo;
import org.safehaus.subutai.core.hostregistry.api.HostRegistry;
import org.safehaus.subutai.core.hostregistry.api.ResourceHostInfo;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.google.common.base.Preconditions;


@Command( scope = "host", name = "list", description = "List hosts" )
public class ListHostsCommand extends OsgiCommandSupport
{
    private final HostRegistry hostRegistry;


    public ListHostsCommand( final HostRegistry hostRegistry )
    {
        Preconditions.checkNotNull( hostRegistry );

        this.hostRegistry = hostRegistry;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        Set<ResourceHostInfo> resourceHostsInfo = hostRegistry.getResourceHostsInfo();

        for ( ResourceHostInfo resourceHostInfo : resourceHostsInfo )
        {
            System.out.println( resourceHostInfo );

            Set<ContainerHostInfo> containerHostInfos = resourceHostInfo.getContainers();

            for ( ContainerHostInfo containerHostInfo : containerHostInfos )
            {
                System.out.println( String.format( "\t%s", containerHostInfo ) );
            }
        }
        return null;
    }
}
