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


    // TODO void addRemoteRepository( URL url );


    String getSerializedPackageInfo( String filename ) throws IllegalArgumentException;


    String getSerializedPackageInfo( byte[] md5 ) throws IllegalArgumentException;

}
