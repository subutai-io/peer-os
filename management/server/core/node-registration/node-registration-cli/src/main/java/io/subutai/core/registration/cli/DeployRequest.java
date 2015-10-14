package io.subutai.core.registration.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.collect.Lists;

import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.registration.api.RegistrationManager;


@Command( scope = "node", name = "deploy", description = "deploy resource host" )
public class DeployRequest extends SubutaiShellCommandSupport
{
    private final RegistrationManager registrationManager;

    @Argument( index = 0, name = "edgeIp", multiValued = false, required = true, description = "edge ip address" )
    private String edgeIp;

    @Argument( index = 1, name = "secret", multiValued = false, required = true, description = "passphrase" )
    private String secret;

    @Argument( index = 2, name = "port", multiValued = false, required = true, description = "SN port",
            valueToShowInHelp = "5000" )
    private String port;

    @Argument( index = 3, name = "name", multiValued = false, required = true, description = "community name" )
    private String name;

    @Argument( index = 4, name = "key", multiValued = false, required = true, description = "name of the key file" )
    private String key;

    @Argument( index = 5, name = "ami", multiValued = false, required = true, description = "AMI name" )
    private String ami;

    @Argument( index = 6, name = "region", multiValued = false, required = true, description = "region" )
    private String region;


    public DeployRequest( final RegistrationManager registrationManager )
    {
        this.registrationManager = registrationManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        registrationManager.deployResourceHost(
                Lists.newArrayList( "-i", edgeIp, "-s", secret, "-p", port, "-n", name, "-k", key, "-a", ami, "-r",
                        region ) );
        return null;
    }
}
