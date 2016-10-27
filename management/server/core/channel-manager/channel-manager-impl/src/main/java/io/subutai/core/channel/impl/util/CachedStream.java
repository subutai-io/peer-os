package io.subutai.core.channel.impl.util;


import java.io.IOException;

import org.apache.cxf.io.CachedOutputStream;


public class CachedStream extends CachedOutputStream
{
    public CachedStream()
    {
        super();
    }


    @Override
    protected void doFlush() throws IOException
    {
        currentStream.flush();
    }


    @Override
    protected void doClose() throws IOException
    {
    }


    @Override
    protected void onWrite() throws IOException
    {
    }
}