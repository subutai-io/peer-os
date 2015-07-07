package io.subutai.core.registry.cli;


import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import io.subutai.core.registry.api.TemplateRegistry;


/**
 * Abstract parent for test classes
 */
public abstract class TestParent
{
    private ByteArrayOutputStream myOut;
    private TemplateRegistry templateRegistry;


    @Before
    public final void before() throws Exception
    {
        myOut = new ByteArrayOutputStream();
        System.setOut( new PrintStream( myOut ) );
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
}
