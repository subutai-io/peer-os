package io.subutai.core.hostregistry.cli;


import java.util.Set;

import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;

import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.HostInterface;
import io.subutai.common.host.ResourceHostInfo;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "host", name = "list", description = "List hosts" )
public class ListHostsCommand extends SubutaiShellCommandSupport
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

        System.out.println("Connected hosts:" );
        for ( ResourceHostInfo resourceHostInfo : resourceHostsInfo )
        {
            System.out.println( String.format( "%s\t%s", resourceHostInfo.getHostname(), resourceHostInfo.getId() ) );

            System.out.println( "\tNetwork interfaces:");
            for ( HostInterface hostInterface : resourceHostInfo.getHostInterfaces().getAll() )
            {
                System.out.println( String.format( "\t\t%s: %s", hostInterface.getName(), hostInterface.getIp() ) );
            }

            System.out.println( "\tContainers:" );
            Set<ContainerHostInfo> containerHostInfos = resourceHostInfo.getContainers();

            for ( ContainerHostInfo containerHostInfo : containerHostInfos )
            {
                System.out.println(
                        String.format( "\t\t%s\t%s\t%s", containerHostInfo.getHostname(), containerHostInfo.getId(),
                                containerHostInfo.getState() ) );
            }
        }
        return null;
    }
}
