package org.safehaus.subutai.core.lxc.quota.cli;


import org.safehaus.subutai.common.quota.QuotaInfo;
import org.safehaus.subutai.core.lxc.quota.api.QuotaManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Created by talas on 12/8/14.
 */
@Command( scope = "quota", name = "set-quota", description = "Sets specified quota to container" )
public class SetQuota extends OsgiCommandSupport
{
    private static final Logger LOGGER = LoggerFactory.getLogger( SetQuota.class );
    private QuotaManager quotaManager;

    @Argument( index = 0, name = "container name", required = true, multiValued = false, description = "specify "
            + "container name" )
    private String containerName;

    @Argument( index = 1, name = "quota type", required = true, multiValued = false, description = "specify quota "
            + "type to set quota" )
    private String quotaType;

    @Argument( index = 2, name = "quota value", required = true, multiValued = false, description = "set quota value" )
    private String quotaValue;


    public SetQuota( QuotaManager quotaManager )
    {
        this.quotaManager = quotaManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        QuotaInfo quotaInfo = new QuotaInfo()
        {
            @Override
            public String getQuotaKey()
            {
                return quotaType;
            }


            @Override
            public String getQuotaValue()
            {
                return quotaValue;
            }
        };
        quotaManager.setQuota( containerName, quotaInfo );
        return null;
    }
}
