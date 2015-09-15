package io.subutai.common.protocol;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.common.datatypes.TemplateVersion;
import io.subutai.common.protocol.Template;
import io.subutai.common.protocol.TemplatePK;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class TemplateTest
{
    private Template template;
    private Template template3;

    @Mock
    TemplateVersion templateVersion;
    @Mock
    TemplatePK templatePK;


    @Before
    public void setUp() throws Exception
    {
        Set<String> mySet = new HashSet<>();
        mySet.add( "master" );

        template = new Template( "testLxcArch", "testSubutaiParent", "testSubutaiConfigPath", "testSubutaiParent",
                "testSubutaiGitBranch", "testSubutaiGitUuid", "testPackagesManifest", "testMad5Sum", templateVersion );

        template3 = new Template( "testLxcArch", "test", "testSubutaiConfigPath", "testSubutaiParent",
                "testSubutaiGitBranch", "testSubutaiGitUuid", "testPackagesManifest", "testMad5Sum", templateVersion );


        template.setPk( templatePK );
        template.setProducts( mySet );
        template.setInUseOnFAI( "testHostName", true );
        templatePK.setMd5sum( "md4sum" );
    }


    @Test
    public void testProperties() throws Exception
    {
        List<Template> myList = new ArrayList<>();
        myList.add( template );
        when( templatePK.getTemplateVersion() ).thenReturn( templateVersion );
        when( templatePK.getLxcArch() ).thenReturn( "LxcArch" );
        when( templatePK.getTemplateName() ).thenReturn( "testTemplate" );
        when( templatePK.getMd5sum() ).thenReturn( "md5sum" );

        assertNotNull( template.getTemplateVersion() );
        assertNotNull( template.getPk() );
        assertNotNull( template.isInUseOnFAIs() );
        assertNotNull( template.getFaisUsingThisTemplate() );
        template.getMd5sum();
        template.addChildren( myList );
        assertNotNull( template.getChildren() );
        assertNotNull( template.getProducts() );
        assertNotNull( template.getLxcArch() );
        assertNotNull( template.getLxcUtsname() );
        assertNotNull( template.getSubutaiConfigPath() );
        assertNotNull( template.getSubutaiGitBranch() );
        assertNotNull( template.getSubutaiParent() );
        assertNotNull( template.getSubutaiGitUuid() );
        assertNotNull( template.getPackagesManifest() );
        assertNotNull( template.getTemplateName() );
        assertNotNull( template.getFileName() );
        assertNotNull( template3.getParentTemplateName() );
        assertNotNull( template.getTemplateVersion() );
        assertNotNull( template.isRemote() );
        assertNotNull( template.getRemoteClone( UUID.randomUUID().toString() ) );
        template.getPeerId();
        template.hashCode();
        template.equals( "test" );
        template.equals( template );
    }
}