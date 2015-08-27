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


    public RequestBuilder getRepositoryCommand( String ip )
    {
        return new RequestBuilder( String.format( "subutai repo add %s", ip ) );
    }


    public RequestBuilder getRemoveRepositoryCommand( String ip )
    {
        return new RequestBuilder( String.format( "subutai repo del %s", ip ) );
    }
}
