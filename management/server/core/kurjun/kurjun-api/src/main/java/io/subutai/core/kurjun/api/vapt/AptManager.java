package io.subutai.core.kurjun.api.vapt;


import java.io.InputStream;
import java.net.URI;
import java.net.URL;


public interface AptManager
{

    String getRelease( String release, String component, String arch );


    InputStream getPackagesIndex( String release, String component, String arch, String packagesIndex ) throws IllegalArgumentException;


    InputStream getPackageByFilename( String filename ) throws IllegalArgumentException;


    String getPackageInfo( byte[] md5, String name, String version );


    InputStream getPackage( byte[] md5 );


    URI upload( InputStream is );


    boolean isCompressionTypeSupported( String packagesIndex );


    /**
     * Adds remote repository located at supplied URL. Repositories added with this method will be used to fulfill
     * requests in case the local repository can not handle requests.
     *
     * @param url URL of the remote repository
     * @param token access token to be used for the given remote repo url
     */
    void addRemoteRepository( URL url, String token );


    /**
     * Removes remote repository located at supplied URL.
     *
     * @param url URL of the remote repository
     */
    void removeRemoteRepository( URL url );


    String getSerializedPackageInfo( String filename ) throws IllegalArgumentException;


    String getSerializedPackageInfo( byte[] md5 ) throws IllegalArgumentException;

}

