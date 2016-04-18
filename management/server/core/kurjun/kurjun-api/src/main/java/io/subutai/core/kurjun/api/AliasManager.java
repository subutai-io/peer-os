package io.subutai.core.kurjun.api;


import ai.subut.kurjun.model.metadata.SerializableMetadata;


public interface AliasManager
{

    SerializableMetadata getMetadataByAliasName( String alias );

    void addAliasName( String alias, String md5 );

    void removeAliasName( String alias );

    String getMd5( String alias );

    String getAlias( String md5 );

}
