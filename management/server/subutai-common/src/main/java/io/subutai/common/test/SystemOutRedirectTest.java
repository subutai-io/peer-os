package io.subutai.common.test;


import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;


/**
 * Abstract parent for test classes wishing to grab system.out
 */
public abstract class SystemOutRedirectTest
{
    private ByteArrayOutputStream myOut;


    @Before
    public final void before()
    {
        resetSysOut();
    }


    @After
    public final void after() throws Exception
    {
        System.setOut( System.out );
    }


    protected String getSysOut()
    {
        return myOut.toString().trim();
    }


    protected void resetSysOut()
    {
        myOut = new ByteArrayOutputStream();
        System.setOut( new PrintStream( myOut ) );
    }
}
