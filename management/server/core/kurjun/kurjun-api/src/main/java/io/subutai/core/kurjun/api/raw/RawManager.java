package io.subutai.core.kurjun.api.raw;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import ai.subut.kurjun.metadata.common.raw.RawMetadata;
import ai.subut.kurjun.model.metadata.SerializableMetadata;


public interface RawManager
{


    InputStream getFile( String repository, byte[] md5 ) throws IOException;

    List<SerializableMetadata> list( String repository ) throws IOException;

    boolean delete( byte[] md5 ) throws IOException;

    boolean delete( String repository, final byte[] md5 );

    String md5();

    RawMetadata getInfo( String repository, byte[] md5 );

    RawMetadata getInfo( final RawMetadata metadata );

    RawMetadata getInfo( final byte[] md5 );

    RawMetadata put( final File file, final String filename, final String repository );

    RawMetadata put( final File file );

    RawMetadata put( final File file, final String repository );
}
