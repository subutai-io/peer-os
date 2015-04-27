package org.safehaus.subutai.core.template.wizard.impl;


import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.peer.Host;

import com.google.common.collect.Lists;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class ProductsInstallationProcedureTest
{
    List<String> products = Lists.newArrayList( "product1", "product2" );
    ProductsInstallationProcedure installationProcedure;

    @Mock
    Host targetHost;

    @Mock
    CommandResult commandResult;


    @Before
    public void setUp() throws Exception
    {
        when( targetHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );
        when( commandResult.getStdOut() ).thenReturn( "Product installation output." );
        installationProcedure = new ProductsInstallationProcedure( products, targetHost );
    }


    @Test
    public void testDoStart() throws Exception
    {
        installationProcedure.doStart();
        verify( commandResult, Mockito.times( 2 ) ).getStdOut();
        verify( targetHost, Mockito.times( 2 ) ).execute( any( RequestBuilder.class ) );
    }


    @Test
    public void testToString() throws Exception
    {
        assertNotNull( installationProcedure.toString() );
    }
}