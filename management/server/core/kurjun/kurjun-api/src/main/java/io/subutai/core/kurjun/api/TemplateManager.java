package io.subutai.core.kurjun.api;


import java.io.IOException;
import java.io.InputStream;
import java.util.List;


/**
 * Template manager interface that wraps Kurjun repositories.
 * <p>
 * This is the first version and further changes will be applied.
 *
 */
public interface TemplateManager
{


    /**
     * Gets template info.
     *
     * @param context repository context
     * @param md5 md5 checksum of the package to retrieve info
     * @return JSON encoded meta data
     * @throws IOException
     */
    String getTemplateInfo( String context, byte[] md5 ) throws IOException;


    /**
     * Gets template info by name and version.
     *
     * @param context repository context
     * @param name name of the package
     * @param version version of the package, may be {@code null}
     * @return JSON encoded meta data
     * @throws IOException
     */
    String getTemplateInfo( String context, String name, String version ) throws IOException;


    /**
     * Gets template stream.
     *
     * @param context repository context
     * @param md5 md5 checksum of the package to retrieve
     * @return input stream to read package data
     * @throws IOException
     */
    InputStream getTemplate( String context, byte[] md5 ) throws IOException;


    /**
     * Lists packages in supplied repository context.
     *
     * @param context repository context
     * @return list of JSON encoded meta data
     * @throws IOException
     */
    List<String> list( String context ) throws IOException;


    /**
     * Uploads package data from supplied input stream to the repository defined by supplied context.
     *
     * @param context repository context
     * @param inputStream input stream to read package data
     * @return md5 checksum of uploaded package upload succeeds; {@code null} otherwise
     * @throws IOException
     */
    byte[] upload( String context, InputStream inputStream ) throws IOException;


    /**
     * Deletes package from the repository defined by supplied context.
     *
     * @param context repository context
     * @param md5 md5 checksum of the package to delete
     * @return {@code true} if package successfully deleted; {@code false} otherwise
     * @throws IOException
     */
    boolean delete( String context, byte[] md5 ) throws IOException;


}

