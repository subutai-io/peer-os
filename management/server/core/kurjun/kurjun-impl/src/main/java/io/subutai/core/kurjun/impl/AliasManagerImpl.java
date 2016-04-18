package io.subutai.core.kurjun.impl;


import java.io.IOException;
import java.util.Collection;

import ai.subut.kurjun.db.file.FileDb;
import ai.subut.kurjun.model.metadata.SerializableMetadata;
import io.subutai.core.kurjun.api.AliasManager;


public class AliasManagerImpl implements AliasManager
{

    FileDb fileDb;

    private final String ALIAS_NAME_MAP = "alias-md5";


    public AliasManagerImpl() throws IOException
    {
        this.fileDb = new FileDb( ALIAS_NAME_MAP );
    }


    @Override
    public SerializableMetadata getMetadataByAliasName( final String alias )
    {

        return null;
    }


    @Override
    public void addAliasName( final String alias, final String md5 )
    {
        this.fileDb.put( ALIAS_NAME_MAP, alias, md5 );
    }


    @Override
    public void removeAliasName( final String alias )
    {
        this.fileDb.get( ALIAS_NAME_MAP ).remove( alias );
    }


    @Override
    public String getMd5( final String alias )
    {
        return ( String ) this.fileDb.get( ALIAS_NAME_MAP ).get( alias );
    }


    @Override
    public String getAlias( final String md5 )
    {
        Collection<Object> names = this.fileDb.get( ALIAS_NAME_MAP ).values();

        for ( Object s : names )
        {
            if ( ( String.valueOf( s ).equalsIgnoreCase( md5 ) ) )
            {
                return ( String ) s;
            }
        }

        return null;
    }
}
