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


    protected RequestBuilder getChangeHostnameInEtcHostsCommand( String oldHostname, String newHostname )
    {
        return new RequestBuilder(
                String.format( "sed -i 's/%1$s/%2$s/g' %3$s", oldHostname, newHostname, Common.ETC_HOSTS_FILE ) );
    }


    protected RequestBuilder getChangeHostnameInAuthorizedKeysCommand( String oldHostname, String newHostname )
    {
        return new RequestBuilder(
                String.format( "chmod 700 %3$s && sed -i 's/%1$s/%2$s/g' %3$s && chmod 644 %3$s", oldHostname,
                        newHostname, Common.CONTAINER_SSH_FILE ) );
    }


    protected RequestBuilder getAddIpHostToEtcHostsCommand( Map<String, String> hostAddresses )
    {
        StringBuilder cleanHosts =
                new StringBuilder( String.format( "%s|%s|", Common.LOCAL_HOST_NAME, Common.LOCAL_HOST_IP ) );
        StringBuilder appendHosts = new StringBuilder();

        for ( Map.Entry<String, String> hostEntry : hostAddresses.entrySet() )
        {
            String hostname = hostEntry.getKey();
            String ip = hostEntry.getValue();
            cleanHosts.append( ip ).append( "|" ).append( hostname ).append( "|" );
            appendHosts.append( "echo '" ).
                    append( ip ).append( " " ).
                               append( hostname ).append( "." ).append( Common.DEFAULT_DOMAIN_NAME ).
                               append( " " ).append( hostname ).
                               append( String.format( "' >> '%s'; ", Common.ETC_HOSTS_FILE ) );
        }

        if ( cleanHosts.length() > 0 )
        {
            //drop pipe | symbol
            cleanHosts.setLength( cleanHosts.length() - 1 );
            cleanHosts.insert( 0, "egrep -v '" );
            cleanHosts.append(
                    String.format( "' %1$s > etc-hosts-cleaned; mv etc-hosts-cleaned %1$s;", Common.ETC_HOSTS_FILE ) );
            appendHosts.insert( 0, cleanHosts );
        }

        appendHosts.append( String.format( "echo '%s %s ", Common.LOCAL_HOST_IP, Common.LOCAL_HOST_NAME ) )
                   .append( String.format( "' >> '%s';", Common.ETC_HOSTS_FILE ) );

        return new RequestBuilder( appendHosts.toString() );
    }


    protected RequestBuilder getReadOrCreateSSHCommand( SshEncryptionType encryptionType )
    {
        return new RequestBuilder( String.format( "if [ -f %1$s/id_%2$s.pub ]; " +
                "then cat %1$s/id_%2$s.pub ;" +
                "else rm -rf %1$s && " +
                "mkdir -p %1$s && " +
                "chmod 700 %1$s && " +
                "ssh-keygen -t %2$s -P '' -f %1$s/id_%2$s -q && " +
                "cat %1$s/id_%2$s.pub; fi", Common.CONTAINER_SSH_FOLDER, encryptionType.name().toLowerCase() ) );
    }


    protected RequestBuilder getReadSSHKeyCommand( SshEncryptionType encryptionType )
    {
        return new RequestBuilder( String.format( "cat %1$s/id_%2$s.pub ", Common.CONTAINER_SSH_FOLDER,
                encryptionType.name().toLowerCase() ) );
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
                "echo '%3$s' >> %2$s && sort -u '%2$s' -o '%2$s' && " +
                "chmod 644 %2$s", Common.CONTAINER_SSH_FOLDER, Common.CONTAINER_SSH_FILE, keys ) );
    }


    protected RequestBuilder getConfigSSHCommand()
    {
        return new RequestBuilder( String.format( "echo 'Host *' > %1$s/config && " +
                "echo '    StrictHostKeyChecking no' >> %1$s/config && " +
                "chmod 644 %1$s/config", Common.CONTAINER_SSH_FOLDER ) );
    }


    protected RequestBuilder getReadAuthorizedKeysFile()
    {
        return new RequestBuilder( String.format( "cat %s", Common.CONTAINER_SSH_FILE ) );
    }
}
