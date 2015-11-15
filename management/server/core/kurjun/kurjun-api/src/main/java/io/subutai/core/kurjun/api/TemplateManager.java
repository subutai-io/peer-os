package io.subutai.core.kurjun.api;


import java.io.InputStream;


public interface TemplateManager
{

    void getTemplate( String repository, String md5, String name, String version, String type );


    void uploadTemplate( String repository, InputStream inputStream );


    void deleteTemplates( String repository, String md5 );
}
