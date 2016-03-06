package io.subutai.core.channel.impl.util;


import java.io.IOException;

import org.apache.cxf.io.CachedOutputStream;


public class CachedStream extends CachedOutputStream
{
    public CachedStream()
    {
        super();
    }


    protected void doFlush() throws IOException
    {
        currentStream.flush();
    }


    protected void doClose() throws IOException
    {
    }


    protected void onWrite() throws IOException
    {
    }
}