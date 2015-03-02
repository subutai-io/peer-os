package org.safehaus.subutai.core.peer.impl;


import java.nio.file.Paths;

import org.safehaus.subutai.common.command.RequestBuilder;

import com.google.common.collect.Lists;


public class Commands
{
    // ManagementHost commands
    public RequestBuilder getAddAptSourceCommand( String hostname, String ip )
    {
        return new RequestBuilder( String.format( "sed '/^path_map.*$/ s/$/ ; %s %s/' apt-cacher.conf > apt-cacher.conf"
                        + ".new && mv apt-cacher.conf.new apt-cacher.conf && /etc/init.d/apt-cacher reload", hostname,
                ( "http://" + ip + "/ksks" ).replace( ".", "\\." ).replace( "/", "\\/" ) ) )
                .withCwd( "/etc/apt-cacher/" );
    }


    public RequestBuilder getRemoveAptSourceCommand( String ip )
    {
        return new RequestBuilder(
                String.format( "sed -e 's,;\\s*[a-f0-9]\\{8\\}-[a-f0-9]\\{4\\}-[a-f0-9]\\{4\\}-[a-f0-9]\\{4" +
                                "\\}-[a-f0-9]\\{12\\}\\s*http:\\/\\/%s/ksks\\s*,,g' apt-cacher.conf > apt-cacher.conf"
                                + ".new && mv apt-cacher.conf.new apt-cacher.conf && /etc/init.d/apt-cacher reload",
                        ip.replace( ".", "\\." ) ) ).withCwd( "/etc/apt-cacher/" );
    }


    public RequestBuilder getDownloadTemplateCommand( String peerIp, int peerPort, String templateName,
                                                      String templateDownloadToken, String downloadDir )
    {
        return new RequestBuilder( "curl" ).withCmdArgs( Lists.newArrayList( "-O", "-J", "-L",
                String.format( "https://%s:%d/cxf/registry/templates/%s/download/%s", peerIp, peerPort, templateName,
                        templateDownloadToken ) ) ).withCwd( downloadDir ).withTimeout( 24 * 60 * 60 );
    }


    public RequestBuilder getCopyTemplateFromManagementHostCommand( String downloadDir, String templateFileName )
    {
        return new RequestBuilder(
                String.format( "scp root@gw.intra.lan:%s", Paths.get( downloadDir, templateFileName ) ) )
                .withTimeout( 180 );
    }


    public RequestBuilder getImportTemplateCommand( String templateName )
    {
        return new RequestBuilder( "subutai" ).withCmdArgs( Lists.newArrayList( "import", templateName ) )
                                              .withTimeout( 3 * 60 );
    }


    public RequestBuilder getCheckTemplateImportedCommand( String templateName )
    {
        return new RequestBuilder( "subutai" ).withCmdArgs( Lists.newArrayList( "list", "-t", templateName ) );
    }


    public RequestBuilder getCheckTemplateDownloadedCommand( String downloadDir, String templateFileName )
    {
        return new RequestBuilder(
                String.format( "mkdir -p %s ; ls %s", downloadDir, Paths.get( downloadDir, templateFileName ) ) );
    }
}
