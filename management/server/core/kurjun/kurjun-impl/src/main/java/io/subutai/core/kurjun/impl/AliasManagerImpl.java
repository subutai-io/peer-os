package io.subutai.core.kurjun.impl;


import java.io.IOException;
import java.util.Collection;

import ai.subut.kurjun.db.file.FileDb;
import io.subutai.core.kurjun.api.AliasManager;


public class AliasManagerImpl implements AliasManager
{

    private FileDb fileDb;

    private final String ALIAS_NAME_MAP = "alias-md5";


    public AliasManagerImpl() throws IOException
    {
        this.fileDb = new FileDb( ALIAS_NAME_MAP );
    }


    @Override
    public synchronized void addAliasName( final String alias, final String md5 )
    {
        this.fileDb.put( ALIAS_NAME_MAP, alias, md5 );
    }


    @Override
    public synchronized void removeAliasName( final String alias )
    {
        this.fileDb.get( ALIAS_NAME_MAP ).remove( alias );
    }


    @Override
    public synchronized String getMd5( final String alias )
    {
        return ( String ) this.fileDb.get( ALIAS_NAME_MAP ).get( alias );
    }


    @Override
    public synchronized String getAlias( final String md5 )
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
