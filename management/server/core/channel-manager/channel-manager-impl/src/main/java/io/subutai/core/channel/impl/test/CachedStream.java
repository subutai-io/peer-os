package io.subutai.core.channel.impl.test;


import java.io.IOException;

import org.apache.cxf.io.CachedOutputStream;


/**
 * Created by dilshat on 9/7/15.
 */
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