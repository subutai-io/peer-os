package io.subutai.core.hostregistry.cli;


import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;

import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.HostInterface;
import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.settings.Common;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "host", name = "list", description = "List hosts" )
public class ListHostsCommand extends SubutaiShellCommandSupport
{
    private final HostRegistry hostRegistry;

    @Argument( index = 0, name = "abbreviate host ids", required = false, multiValued = false, description =
            "abbreviate host ids (true/false)" )
    boolean abbreviate = true;


    public ListHostsCommand( final HostRegistry hostRegistry )
    {
        Preconditions.checkNotNull( hostRegistry );

        this.hostRegistry = hostRegistry;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        Set<ResourceHostInfo> resourceHostsInfo = hostRegistry.getResourceHostsInfo();

        System.out.println( "Connected hosts:" );
        for ( ResourceHostInfo resourceHostInfo : resourceHostsInfo )
        {
            System.out.println( String.format( "%s\t%s", resourceHostInfo.getHostname(),
                    abbreviate ? StringUtils.abbreviate( resourceHostInfo.getId(), 7 ) : resourceHostInfo.getId() ) );

            System.out.println( "\tNetwork interfaces:" );
            for ( HostInterface hostInterface : resourceHostInfo.getHostInterfaces().getAll() )
            {
                System.out.println( String.format( "\t\t%s: %s", hostInterface.getName(), hostInterface.getIp() ) );
            }

            System.out.println( "\tContainers:" );
            Set<ContainerHostInfo> containerHostInfos = resourceHostInfo.getContainers();

            for ( ContainerHostInfo containerHostInfo : containerHostInfos )
            {
                System.out.println( String.format( "\t\t%s [%s]\t%s\t%s\t%s", containerHostInfo.getHostname(),
                        containerHostInfo.getContainerName(),
                        abbreviate ? StringUtils.abbreviate( containerHostInfo.getId(), 7 ) : containerHostInfo.getId(),
                        containerHostInfo.getHostInterfaces().findByName( Common.DEFAULT_CONTAINER_INTERFACE ).getIp(),
                        containerHostInfo.getState() ) );
            }

            System.out.println( "-------" );
        }
        return null;
    }
}
