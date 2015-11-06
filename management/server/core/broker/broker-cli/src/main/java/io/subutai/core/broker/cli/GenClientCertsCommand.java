package io.subutai.core.broker.cli;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;

import io.subutai.common.command.CommandException;
import io.subutai.core.broker.api.Broker;
import io.subutai.core.broker.api.BrokerException;
import io.subutai.core.broker.api.ClientCredentials;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "broker", name = "gen-client-certs", description = "Generates new client SSL cert and key" )
public class GenClientCertsCommand extends SubutaiShellCommandSupport
{

    private static final Logger LOG = LoggerFactory.getLogger( GenClientCertsCommand.class.getName() );

    private final Broker broker;

    @Argument( index = 0, name = "client id", required = true, multiValued = false, description = "Client id" )
    String clientId;
    @Argument( index = 1, name = "client key password", required = true, multiValued = false, description = "Password"
            + " for client private key" )
    String privateKeyPwd;


    public GenClientCertsCommand( final Broker broker )
    {
        Preconditions.checkNotNull( broker );

        this.broker = broker;
    }


    @Override
    protected Object doExecute() throws CommandException
    {

        try
        {
            ClientCredentials clientCredentials = broker.createNewClientCredentials( clientId, privateKeyPwd );

            System.out.println( "Client certificate:\n" );
            System.out.println( clientCredentials.getClientCertificate() );
            System.out.println( "Client private key:\n" );
            System.out.println( clientCredentials.getClientKey() );
            System.out.println( "CA certificate:\n" );
            System.out.println( clientCredentials.getCaCertificate() );
        }
        catch ( BrokerException e )
        {
            LOG.error( "Error creating new client credentials on broker", e );
            System.out.println( e.getMessage() );
        }
        return null;
    }
}
