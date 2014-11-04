package org.safehaus.subutai.core.script.api;


import java.io.InputStream;


/**
 * Script Manager allows to upload and download per & post execution scripts These scripts are executed during subutai
 * container management operations like subutai clone master foo
 */
public interface ScriptManager
{

    public void importScript( InputStream inputStream, String filename );
}
