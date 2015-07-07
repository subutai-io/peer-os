package org.safehaus.subutai.core.lxc.quota.cli;


import org.safehaus.subutai.common.quota.QuotaType;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;

import org.apache.karaf.shell.commands.Command;


@Command( scope = "quota", name = "list-quota", description = "list all available quotas" )
public class ListQuotaTypes extends SubutaiShellCommandSupport
{
    @Override
    protected Object doExecute() throws Exception
    {
        for ( final QuotaType quotaType : QuotaType.values() )
        {
            System.out.println( quotaType.getKey() );
        }
        return null;
    }
}
