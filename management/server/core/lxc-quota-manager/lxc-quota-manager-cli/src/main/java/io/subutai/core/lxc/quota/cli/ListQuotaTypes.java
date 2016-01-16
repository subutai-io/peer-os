package io.subutai.core.lxc.quota.cli;


import org.apache.karaf.shell.commands.Command;

import io.subutai.common.resource.ContainerResourceType;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "quota", name = "list", description = "list all available resource types" )
public class ListQuotaTypes extends SubutaiShellCommandSupport
{
    @Override
    protected Object doExecute() throws Exception
    {
        for ( final ContainerResourceType containerResourceType : ContainerResourceType.values() )
        {
            System.out.println( String.format( "%s\t%s", containerResourceType, containerResourceType.getKey() ) );
        }
        return null;
    }
}
