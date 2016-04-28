package io.subutai.core.localpeer.impl;


import java.util.Map;

import io.subutai.common.command.RequestBuilder;
import io.subutai.common.security.SshEncryptionType;
import io.subutai.common.settings.Common;


public class LocalPeerCommands
{
    public RequestBuilder getExchangeKeyCommand( String hostname, String token )
    {
        return new RequestBuilder( String.format( "subutai import %s -t %s", hostname, token ) );
    }


    protected RequestBuilder getAddIpHostToEtcHostsCommand( Map<String, String> hostAddresses )
    {
        StringBuilder cleanHosts = new StringBuilder( "localhost|127.0.0.1|" );
        StringBuilder appendHosts = new StringBuilder();

        for ( Map.Entry<String, String> hostEntry : hostAddresses.entrySet() )
        {
            String hostname = hostEntry.getKey();
            String ip = hostEntry.getValue();
            cleanHosts.append( ip ).append( "|" ).append( hostname ).append( "|" );
            appendHosts.append( "/bin/echo '" ).
                    append( ip ).append( " " ).
                               append( hostname ).append( "." ).append( Common.DEFAULT_DOMAIN_NAME ).
                               append( " " ).append( hostname ).
                               append( "' >> '/etc/hosts'; " );
        }

        if ( cleanHosts.length() > 0 )
        {
            //drop pipe | symbol
            cleanHosts.setLength( cleanHosts.length() - 1 );
            cleanHosts.insert( 0, "egrep -v '" );
            cleanHosts.append( "' /etc/hosts > etc-hosts-cleaned; mv etc-hosts-cleaned /etc/hosts;" );
            appendHosts.insert( 0, cleanHosts );
        }

        appendHosts.append( "/bin/echo '127.0.0.1 localhost " ).append( "' >> '/etc/hosts';" );

        return new RequestBuilder( appendHosts.toString() );
    }


    protected RequestBuilder getReadOrCreateSSHCommand()
    {
        return new RequestBuilder( String.format( "if [ -f %1$s/id_dsa.pub ]; " +
                "then cat %1$s/id_dsa.pub ;" +
                "else rm -rf %1$s && " +
                "mkdir -p %1$s && " +
                "chmod 700 %1$s && " +
                "ssh-keygen -t dsa -P '' -f %1$s/id_dsa -q && " +
                "cat %1$s/id_dsa.pub; fi", Common.CONTAINER_SSH_FOLDER ) );
    }


    protected RequestBuilder getReadSSHKeyCommand( SshEncryptionType encryptionType )
    {
        return new RequestBuilder( String.format( "cat %1$s/id_%2$s.pub ", Common.CONTAINER_SSH_FOLDER,
                encryptionType.name().toLowerCase() ) );
    }


    protected RequestBuilder getCreateSSHKeyCommand( SshEncryptionType encryptionType )
    {
        return new RequestBuilder( String.format( "rm -rf %1$s && " +
                "mkdir -p %1$s && " +
                "chmod 700 %1$s && " +
                "ssh-keygen -t %2$s -P '' -f %1$s/id_%2$s -q && " +
                "cat %1$s/id_%2$s.pub", Common.CONTAINER_SSH_FOLDER, encryptionType.name().toLowerCase() ) );
    }


    public RequestBuilder getAppendSshKeyCommand( String key )
    {
        return new RequestBuilder( String.format(
                "mkdir -p '%1$s' && " + "echo '%3$s' >> '%2$s' && " + "chmod 700 -R '%1$s' && "
                        + "sort -u '%2$s' -o '%2$s'", Common.CONTAINER_SSH_FOLDER, Common.CONTAINER_SSH_FILE, key ) );
    }


    protected RequestBuilder getRemoveSshKeyCommand( final String key )
    {
        return new RequestBuilder( String.format( "chmod 700 %1$s && " +
                "sed -i \"\\,%3$s,d\" %2$s && " +
                "chmod 644 %2$s", Common.CONTAINER_SSH_FOLDER, Common.CONTAINER_SSH_FILE, key ) );
    }


    protected RequestBuilder getAppendSshKeysCommand( String keys )
    {
        return new RequestBuilder( String.format( "mkdir -p %1$s && " +
                "chmod 700 %1$s && " +
                "echo '%3$s' >> %2$s && " +
                "chmod 644 %2$s", Common.CONTAINER_SSH_FOLDER, Common.CONTAINER_SSH_FILE, keys ) );
    }


    protected RequestBuilder getConfigSSHCommand()
    {
        return new RequestBuilder( String.format( "echo 'Host *' > %1$s/config && " +
                "echo '    StrictHostKeyChecking no' >> %1$s/config && " +
                "chmod 644 %1$s/config", Common.CONTAINER_SSH_FOLDER ) );
    }
}
