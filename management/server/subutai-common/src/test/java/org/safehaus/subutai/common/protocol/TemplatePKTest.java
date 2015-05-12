package org.safehaus.subutai.common.protocol;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.datatypes.TemplateVersion;

import static org.junit.Assert.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class TemplatePKTest
{
    private TemplatePK templatePK;


    @Mock
    TemplateVersion templateVersion;


    @Before
    public void setUp() throws Exception
    {
        templatePK = new TemplatePK( "testTemplate", "lxcArch", templateVersion, "md5sum" );
        templatePK.setMd5sum( "md5sum" );
        templatePK.setLxcArch( "lxcArch" );
        templatePK.setTemplateName( "testTemplate" );
        templatePK.setTemplateVersion( templateVersion );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertNotNull( templatePK.getTemplateVersion() );
        assertNotNull( templatePK.getTemplateName() );
        assertNotNull( templatePK.getLxcArch() );
        assertNotNull( templatePK.getMd5sum() );
        assertNotNull( templatePK.hashCode() );
        templatePK.toString();
        templatePK.equals( "test" );
        templatePK.equals( templatePK );
    }
}