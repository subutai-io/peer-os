package io.subutai.core.environment.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;

import io.subutai.common.network.ProxyLoadBalanceStrategy;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


/**
 * Destroys environment container
 */
@Command( scope = "environment", name = "add-domain", description = "Adds domain to environment" )
public class AddDomainToEnvironmentCommand extends SubutaiShellCommandSupport
{
    @Argument( name = "envId", description = "Environment id",
            index = 0, multiValued = false, required = true )
    String envId;
    @Argument( name = "domain", description = "Environment domain",
            index = 1, multiValued = false, required = true )
    String domain;


    private final EnvironmentManager environmentManager;


    public AddDomainToEnvironmentCommand( final EnvironmentManager environmentManager )
    {
        Preconditions.checkNotNull( environmentManager );

        this.environmentManager = environmentManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {

        environmentManager.assignEnvironmentDomain( envId, domain, ProxyLoadBalanceStrategy.ROUND_ROBIN, null );

        System.out.println( "Domain is added to environment" );

        return null;
    }
}
