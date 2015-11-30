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

    //-O RSA access key
    @Argument( index = 0, name = "key", multiValued = false, required = true, description = "RSA access key" )
    private String key;

    //-W RSA security key
    @Argument( index = 1, name = "secret", multiValued = false, required = true, description = "RSA Security key " )
    private String secret;

    //-k RSA key name
    @Argument( index = 2, name = "name", multiValued = false, required = true, description = "RSA key name Id" )
    private String name;

    //-a AMI name
    @Argument( index = 3, name = "ami", multiValued = false, required = true, description = "AMI image name" )
    private String ami;

    //-r Region
    @Argument( index = 4, name = "region", multiValued = false, required = true, description = "AWS region" )
    private String region;

    //-s N2N password
    @Argument( index = 5, name = "password", multiValued = false, required = true, description = "N2N password" )
    private String pass;



    public DeployRequest( final RegistrationManager registrationManager )
    {
        this.registrationManager = registrationManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        registrationManager.deployResourceHost(Lists.newArrayList("-O",key,"-W",secret,"-k",name,"-a",ami,"-r",region,"-s",pass) );

        return null;
    }
}
