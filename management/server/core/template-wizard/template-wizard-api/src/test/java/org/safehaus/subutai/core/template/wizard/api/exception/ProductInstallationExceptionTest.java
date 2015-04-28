package org.safehaus.subutai.core.template.wizard.api.exception;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;


/**
 * Created by talas on 4/28/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class ProductInstallationExceptionTest
{

    ProductInstallationException productInstallationException;


    @Test
    public void testConstructor() throws Exception
    {
        productInstallationException = new ProductInstallationException();
        assertNotNull( productInstallationException );
    }


    @Test
    public void testConstructorWithParam1() throws Exception
    {
        productInstallationException = new ProductInstallationException( "message" );
        assertNotNull( productInstallationException );
    }


    @Test
    public void testConstructorWithParam2() throws Exception
    {
        productInstallationException = new ProductInstallationException( "message", new Throwable() );
        assertNotNull( productInstallationException );
    }


    @Test
    public void testConstructorWithParam3() throws Exception
    {
        productInstallationException = new ProductInstallationException( new Throwable() );
        assertNotNull( productInstallationException );
    }


    @Test
    public void testConstructorWithParam4() throws Exception
    {
        productInstallationException = new ProductInstallationException( "message", new Throwable(), true, true );
        assertNotNull( productInstallationException );
    }
}