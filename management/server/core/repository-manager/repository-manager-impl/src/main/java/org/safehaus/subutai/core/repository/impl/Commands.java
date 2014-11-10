package org.safehaus.subutai.core.repository.impl;


import org.safehaus.subutai.common.protocol.RequestBuilder;


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


    public RequestBuilder getPackageInfoCommand( String packageName )
    {
        return new RequestBuilder( String.format( "subutai package_manager info %s", packageName ) );
    }
}
