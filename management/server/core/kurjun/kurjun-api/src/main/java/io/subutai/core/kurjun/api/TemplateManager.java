package io.subutai.core.kurjun.api;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import io.subutai.common.protocol.TemplateKurjun;
import java.util.Set;


/**
 * TemplateKurjun manager interface that wraps Kurjun repositories. <p> This is the first version and further changes
 * will be applied.
 */
public interface TemplateManager
{

    /**
     * Name of public repository (context)
     */
    public static final String PUBLIC_REPO = "public";

    /**
     * Gets template info.
     *
     * @param context repository context
     * @param md5 md5 checksum of the package to retrieve info
     * @param isKurjunClient where the client is Kurjun or not
     *
     * @return JSON encoded meta data
     */
    TemplateKurjun getTemplate( String context, byte[] md5, boolean isKurjunClient ) throws IOException;


    /**
     * Gets template info by name and version.
     *
     * @param context repository context
     * @param name name of the package
     * @param version version of the package, may be {@code null}
     * @param isKurjunClient where the client is Kurjun or not
     *
     * @return JSON encoded meta data
     */
    TemplateKurjun getTemplate( String context, String name, String version, boolean isKurjunClient ) throws IOException;

    /**
     * Gets template info by name, The version is ignored, the repository is public 
     * and treated as not Kurjun client
     *
     * @param name name of the package
     *
     * @return JSON encoded meta data
     */
    TemplateKurjun getTemplate( String name );
    
    /**
     * Gets the list of remote repo urls
     *
     * @return Set of urls
     */
    List<URL> getRemoteRepoUrls();


    /**
     * Gets template stream.
     *
     * @param context repository context
     * @param md5 md5 checksum of the package to retrieve
     * @param isKurjunClient where the client is Kurjun or not
     *
     * @return input stream to read package data
     */
    InputStream getTemplateData( String context, byte[] md5, boolean isKurjunClient ) throws IOException;


    /**
     * Lists packages in supplied repository context.
     *
     * @param context repository context
     * @param isKurjunClient where the client is Kurjun or not
     *
     * @return list of JSON encoded meta data
     */
    List<TemplateKurjun> list( String context, boolean isKurjunClient ) throws IOException;

    /**
     * Lists packages in public repository context.
     * The request treated as not kurjun client.
     *
     * @return list of JSON encoded meta data
     */
    List<TemplateKurjun> list();


    /**
     * Uploads package data from supplied input stream to the repository defined by supplied context.
     *
     * @param context repository context
     * @param inputStream input stream to read package data
     *
     * @return md5 checksum of uploaded package upload succeeds; {@code null} otherwise
     */
    byte[] upload( String context, InputStream inputStream ) throws IOException;


    /**
     * Deletes package from the repository defined by supplied context.
     *
     * @param context repository context
     * @param md5 md5 checksum of the package to delete
     *
     * @return {@code true} if package successfully deleted; {@code false} otherwise
     */
    boolean delete( String context, byte[] md5 ) throws IOException;


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
}

