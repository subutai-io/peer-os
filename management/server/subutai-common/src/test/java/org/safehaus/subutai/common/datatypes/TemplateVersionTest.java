package org.safehaus.subutai.common.datatypes;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class TemplateVersionTest
{
    private TemplateVersion templateVersion;


    @Before
    public void setUp() throws Exception
    {
        templateVersion = new TemplateVersion( "555" );
    }


    @Test
    public void testGetTemplateVersion() throws Exception
    {
        assertNotNull( templateVersion.getTemplateVersion() );
    }


    @Test
    public void testToString() throws Exception
    {
        assertNotNull( templateVersion.toString() );
    }


    @Test
    public void testEquals() throws Exception
    {
        templateVersion.equals( "test" );
    }


    @Test
    public void testEquals2() throws Exception
    {
        templateVersion.equals( templateVersion );
    }


    @Test
    public void testHashCode() throws Exception
    {
        templateVersion.hashCode();
    }
}