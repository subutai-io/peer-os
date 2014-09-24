package org.safehaus.subutai.core.monitor.ui.util;


import org.safehaus.subutai.common.util.FileUtil;

import com.vaadin.ui.Window;


public class JavaScript
{

    private Window window;


    public JavaScript( Window window )
    {
        this.window = window;
    }


    public void loadFile( String filePath )
    {
        execute( FileUtil.getContent( filePath, this ) );
    }


    public void execute( String code )
    {
        //        window.executeJavaScript(code);
    }
}
