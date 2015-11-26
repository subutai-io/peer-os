package io.subutai.core.lxc.quota.cli;


import org.apache.karaf.shell.commands.Command;

import io.subutai.common.resource.ResourceType;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "quota", name = "list", description = "list all available resource types" )
public class ListQuotaTypes extends SubutaiShellCommandSupport
{
    @Override
    protected Object doExecute() throws Exception
    {
        for ( final ResourceType resourceType : ResourceType.values() )
        {
            System.out.println( String.format( "%s\t%s", resourceType, resourceType.getKey() ) );
        }
        return null;
    }
}
