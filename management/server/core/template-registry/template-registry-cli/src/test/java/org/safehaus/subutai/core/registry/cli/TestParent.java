package org.safehaus.subutai.core.registry.cli;


import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;


/**
 * Abstract parent for test classes
 */
public abstract class TestParent
{
    private ByteArrayOutputStream myOut;
    private TemplateRegistry templateRegistry;


    @Before
    public void setUp() throws Exception
    {
        myOut = new ByteArrayOutputStream();
        System.setOut( new PrintStream( myOut ) );
    }


    @After
    public void tearDown() throws Exception
    {
        System.setOut( System.out );
    }


    private String getSysOut()
    {
        return myOut.toString().trim();
    }
}
