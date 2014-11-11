package org.safehaus.subutai.core.executor.impl;


import org.junit.Test;


public class DummyCallbackTest
{

    @Test
    public void testOnResponse() throws Exception
    {
        DummyCallback sut = new DummyCallback();

        sut.onResponse( null, null );
    }
}
