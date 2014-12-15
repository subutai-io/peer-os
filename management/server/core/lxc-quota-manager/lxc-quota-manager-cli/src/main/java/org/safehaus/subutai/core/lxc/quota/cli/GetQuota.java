package org.safehaus.subutai.core.lxc.quota.cli;


import org.safehaus.subutai.common.quota.PeerQuotaInfo;
import org.safehaus.subutai.common.quota.QuotaType;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.lxc.quota.api.QuotaException;
import org.safehaus.subutai.core.lxc.quota.api.QuotaManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Created by talas on 12/8/14.
 */
@Command( scope = "quota", name = "get-quota", description = "Gets quota for specified container" )
public class GetQuota extends OsgiCommandSupport
{
    private static final Logger LOGGER = LoggerFactory.getLogger( GetQuota.class );
    @Argument( index = 0, name = "container name", required = true, multiValued = false,
            description = "container name" )
    private String containerName;

    @Argument( index = 1, name = "quota type", required = true, multiValued = false,
            description = "quota type to set specific quota" )
    private String quotaType;

    private QuotaManager quotaManager;


    public GetQuota( QuotaManager quotaManager )
    {
        this.quotaManager = quotaManager;
    }


    @Override
    protected Object doExecute()
    {
        try
        {
            PeerQuotaInfo quotaInfo = quotaManager.getQuota( containerName, QuotaType.getQuotaType( quotaType ) );
            System.out.println( quotaInfo );
            LOGGER.info( JsonUtil.toJson( quotaInfo ) );
        }
        catch ( QuotaException e )
        {
            System.out.println( "Error getting quota for container" );
            LOGGER.error( "Error getting quota for container", e );
        }
        return null;
    }
}
