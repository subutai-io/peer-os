package org.safehaus.subutai.core.peer.impl;


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


    public RequestBuilder getDownloadTemplateCommand( String peerIp, int peerPort, String templateName )
    {
        return new RequestBuilder( "curl" ).withCmdArgs( Lists.newArrayList( "-O", "-J", "-L",
                String.format( "http://%s:%d/cxf/registry/templates/%s/download ", peerIp, peerPort, templateName ) ) )
                                           .withCwd( "/lxc-data/tmpdir" ).withTimeout( 24 * 60 * 60 );
    }


    public RequestBuilder getImportTemplateCommand( String templateName )
    {
        return new RequestBuilder( "subutai" ).withCmdArgs( Lists.newArrayList( "import", templateName ) )
                                              .withTimeout( 3 * 60 );
    }


    public RequestBuilder getCheckTemplateCommand( String templateName )
    {
        return new RequestBuilder( "subutai" ).withCmdArgs( Lists.newArrayList( "list", "-t", templateName ) );
    }
}
