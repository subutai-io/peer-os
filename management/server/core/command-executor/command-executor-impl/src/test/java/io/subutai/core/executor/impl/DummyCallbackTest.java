package io.subutai.core.executor.impl;


import org.junit.Test;

import io.subutai.core.executor.impl.DummyCallback;


public class DummyCallbackTest
{

    @Test
    public void testOnResponse() throws Exception
    {
        DummyCallback sut = new DummyCallback();

        sut.onResponse( null, null );
    }
}
