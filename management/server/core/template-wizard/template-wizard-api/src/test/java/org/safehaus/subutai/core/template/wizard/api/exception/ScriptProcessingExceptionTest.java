package org.safehaus.subutai.core.template.wizard.api.exception;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;


/**
 * Created by talas on 4/28/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class ScriptProcessingExceptionTest
{
    ScriptProcessingException processingException;


    @Test
    public void testConstructor()
    {
        processingException = new ScriptProcessingException();
        assertNotNull( processingException );
    }


    @Test
    public void testConstructorWithParam1()
    {
        processingException = new ScriptProcessingException( "message" );
        assertNotNull( processingException );
    }


    @Test
    public void testConstructorWithParam2()
    {
        processingException = new ScriptProcessingException( "message", new Throwable() );
        assertNotNull( processingException );
    }


    @Test
    public void testConstructorWithParam3()
    {
        processingException = new ScriptProcessingException( new Throwable() );
        assertNotNull( processingException );
    }


    @Test
    public void testConstructorWithParam4()
    {
        processingException = new ScriptProcessingException( "message", new Throwable(), true, true );
        assertNotNull( processingException );
    }
}