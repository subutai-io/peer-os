package io.subutai.core.identity.ui;


import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.core.identity.ui.ErrorUtils;


@RunWith( MockitoJUnitRunner.class )
public class ErrorUtilsTest
{
    private ErrorUtils errorUtils;


    @Before
    public void setUp() throws Exception
    {
        errorUtils = new ErrorUtils();
    }


    @Test
    public void testGetComponentError() throws Exception
    {
        errorUtils.getComponentError( new ArrayList<Object>() );
    }
}