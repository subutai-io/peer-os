package org.safehaus.subutai.common.security;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class ShiroPrincipalTest
{
    private ShiroPrincipal shiroPrincipal;

    @Mock
    SubutaiLoginContext subutaiLoginContext;


    @Before
    public void setUp() throws Exception
    {
        shiroPrincipal = new ShiroPrincipal( subutaiLoginContext );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertNotNull( shiroPrincipal.getSubutaiLoginContext() );
        assertNotNull( shiroPrincipal.getName() );
    }
}