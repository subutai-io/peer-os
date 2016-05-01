package io.subutai.core.kurjun.api;


public interface AliasManager
{

    void addAliasName( String alias, String md5 );

    void removeAliasName( String alias );

    String getMd5( String alias );

    String getAlias( String md5 );

}
