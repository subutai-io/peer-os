package io.subutai.core.kurjun.api.raw;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import ai.subut.kurjun.model.metadata.SerializableMetadata;
//import ai.subut.kurjun.model.metadata.Metadata;


public interface RawManager
{


    InputStream getFile( String repository, byte[] md5 ) throws IOException;

    List<SerializableMetadata> list( String repository ) throws IOException;

    boolean delete( byte[] md5 ) throws IOException;

    boolean delete( String repository, final byte[] md5 );

    String md5();

    Object getInfo( String repository, byte[] md5 );

    Object getInfo( String repository, String name, String version, byte[]md5 );

    Object getInfo( final Object metadata );

    Object getInfo( final byte[] md5 );

    Object put( final File file, final String filename, final String repository );

    Object put( final File file );

    Object put( final File file, final String repository );
}
