package io.subutai.core.repository.impl;


import java.util.Set;

import io.subutai.common.command.RequestBuilder;
import io.subutai.common.util.StringUtil;


/**
 * Repository Commands
 */
public class Commands
{
    public RequestBuilder getAddPackageCommand( String pathToPackage )
    {
        return new RequestBuilder( String.format( "subutai package_manager add %s", pathToPackage ) );
    }


    public RequestBuilder getRemovePackageCommand( String packageName )
    {
        return new RequestBuilder( String.format( "subutai package_manager remove %s", packageName ) );
    }


    public RequestBuilder getListPackagesCommand( String term )
    {
        return new RequestBuilder( String.format( "subutai package_manager list %s", term ) );
    }


    public RequestBuilder getExtractPackageCommand( String packageName )
    {
        return new RequestBuilder( String.format( "subutai package_manager extract %s", packageName ) );
    }


    public RequestBuilder getExtractFilesCommand( String packageName, Set<String> files )
    {
        return new RequestBuilder( String.format( "subutai package_manager extract %s -f %s", packageName,
                StringUtil.joinStrings( files, ',', false ) ) );
    }


    public RequestBuilder getPackageInfoCommand( String packageName )
    {
        return new RequestBuilder( String.format( "subutai package_manager info %s", packageName ) );
    }


    public RequestBuilder getUpdateRepoCommand()
    {
        return new RequestBuilder( "apt-get update" ).withTimeout( 120 );
    }


    //TODO remove all system specific command and paths, use a dedicated binding for this
    public RequestBuilder getAddAptSourceCommand( String hostname, String ip )
    {
        return new RequestBuilder( String.format( "sed '/^path_map.*$/ s/$/ ; %s %s/' apt-cacher.conf > apt-cacher.conf"
                        + ".new && mv apt-cacher.conf.new apt-cacher.conf && /etc/init.d/apt-cacher reload", hostname,
                ( "http://" + ip + "/ksks" ).replace( ".", "\\." ).replace( "/", "\\/" ) ) )
                .withCwd( "/etc/apt-cacher/" );
    }


    //TODO remove all system specific command and paths, use a dedicated binding for this
    public RequestBuilder getRemoveAptSourceCommand( String ip )
    {
        return new RequestBuilder(
                String.format( "sed -e 's,;\\s*[a-f0-9]\\{8\\}-[a-f0-9]\\{4\\}-[a-f0-9]\\{4\\}-[a-f0-9]\\{4" +
                                "\\}-[a-f0-9]\\{12\\}\\s*http:\\/\\/%s/ksks\\s*,,g' apt-cacher.conf > apt-cacher.conf"
                                + ".new && mv apt-cacher.conf.new apt-cacher.conf && /etc/init.d/apt-cacher reload",
                        ip.replace( ".", "\\." ) ) ).withCwd( "/etc/apt-cacher/" );
    }
}
