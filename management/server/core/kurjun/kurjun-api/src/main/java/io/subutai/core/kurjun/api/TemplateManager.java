package io.subutai.core.kurjun.api;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import io.subutai.common.protocol.TemplateKurjun;


/**
 * TemplateKurjun manager interface that wraps Kurjun repositories.
 * <p>
 * This is the first version and further changes will be applied.
 * </p>
 */
public interface TemplateManager extends QuotaManagedRepository
{

    /**
     * Gets template info.
     *
     * @param repository repository
     * @param md5 md5 checksum of the package to retrieve info
     * @param templateOwner template owner
     * @param isKurjunClient where the client is Kurjun or not
     *
     * @return JSON encoded meta data
     */
    TemplateKurjun getTemplate( String repository, byte[] md5, String templateOwner, boolean isKurjunClient ) throws IOException;


    /**
     * Gets template info by name and version.
     *
     * @param repository repository
     * @param name name of the package
     * @param version version of the package, may be {@code null}
     * @param isKurjunClient where the client is Kurjun or not
     *
     * @return JSON encoded meta data
     */
    TemplateKurjun getTemplate( String repository, String name, String version, boolean isKurjunClient ) throws IOException;


    /**
     * Gets template info by name, The version is ignored, the repository is public and treated as not Kurjun client
     *
     * @param name name of the package
     *
     * @return JSON encoded meta data
     */
    TemplateKurjun getTemplate( String name );


//    /**
//     * Gets the list of remote repo urls
//     *
//     * @return Set of urls
//     */
//    List<Map<String, Object>> getRemoteRepoUrls();
//

    /**
     * Gets template stream.
     *
     * @param repository repository
     * @param md5 md5 checksum of the package to retrieve
     * @param templateOwner template owner
     * @param isKurjunClient where the client is Kurjun or not
     *
     * @return input stream to read package data
     */
    InputStream getTemplateData( String repository, byte[] md5, String templateOwner, boolean isKurjunClient ) throws IOException;


    /**
     * Lists packages in supplied repository.
     *
     * @param repository repository
     * @param isKurjunClient where the client is Kurjun or not
     *
     * @return list of JSON encoded meta data
     */
    List<TemplateKurjun> list( String repository, boolean isKurjunClient ) throws IOException;

//
//    List<Map<String, Object>> getSharedTemplateInfos( byte[] md5, String templateOwner ) throws IOException;
//
//
//    List<Map<String, Object>> listAsSimple( String repository ) throws IOException;
//

    /**
     * Lists packages in public repository. The request treated as not kurjun client.
     *
     * @return list of JSON encoded meta data
     */
    List<TemplateKurjun> list();


    /**
     * Checks whether current active user session can do upload to the given repository.
     *
     * @param repository
     * @return
     */
//    boolean isUploadAllowed( String repository );


    /**
     * Uploads package data from supplied input stream to the repository defined by supplied repository.
     *
     * @param repository repository
     * @param inputStream input stream to read package data
     *
     * @return template id of uploaded package upload succeeds; {@code null} otherwise
     */
    String upload( String repository, InputStream inputStream ) throws IOException;


    /**
     * Deletes package from the repository defined by supplied repository.
     *
     * @param repository repository
     * @param templateOwner template owner
     * @param md5 md5 checksum of the package to delete
     *
     * @return {@code true} if package successfully deleted; {@code false} otherwise
     */
    boolean delete( String repository, String templateOwner, byte[] md5 ) throws IOException;


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


    /**
     * Gets the set of repositories
     *
     * @return
     */
//    Set<String> getRepositories();
    
    
    /**
     * Create repository for the user with the given user name
     * @param userName 
     */
//    void createUserRepository( String userName );


    /**
     * Shares given template to given target user by current active user
     *
     * @param templateId template id
     * @param targetUserName target username
     */
//    void shareTemplate( String templateId, String targetUserName );


    /**
     * Deletes the share for given template to given target user by current active user
     *
     * @param templateId template id
     * @param targetUserName target username
     */
//    void unshareTemplate( String templateId, String targetUserName );
    
}
