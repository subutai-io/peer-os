package io.subutai.core.kurjun.api.raw;


import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import io.subutai.common.protocol.Resource;


public interface RawManager
{

    Resource getFile( byte[] md5, boolean isKurjunClient ) throws IOException;


    Resource getFile( String name, boolean isKurjunClient ) throws IOException;


    InputStream getFileData( byte[] md5, boolean isKurjunClient ) throws IOException;


    List<Resource> getFileList( boolean isKurjunClient ) throws IOException;


    String uploadFile( InputStream inputStream, String fileName ) throws IOException;


    boolean deleteFile( byte[] md5 ) throws IOException;

}
