package io.subutai.core.registry.api;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.Template;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;


/**
 * Test for Template
 */
public class TemplateTest
{

    private static final String FAI_HOSTNAME = "hostname";
    private static final String EXCEPTION_MSG = "exception";
    private static final Set<String> PRODUCTS = Sets.newHashSet( "product" );
    private Template template;


    @Before
    public void setUp() throws Exception
    {
        template = TestUtils.getDefaultTemplate();
    }


    @Test
    public void testMd5Sum() throws Exception
    {
        assertEquals( TestUtils.MD_5_SUM, template.getMd5sum() );
    }


    @Test
    public void testGetParent() throws Exception
    {
        assertNull( template.getParentTemplateName() );
    }


    @Test
    public void testUseOnFais() throws Exception
    {
        assertFalse( template.isInUseOnFAIs() );

        template.setInUseOnFAI( FAI_HOSTNAME, true );

        assertTrue( template.isInUseOnFAIs() );

        Assert.assertTrue( template.getFaisUsingThisTemplate().contains( FAI_HOSTNAME ) );
    }


    @Test
    public void testGetProducts() throws Exception
    {
        template.setProducts( PRODUCTS );

        assertEquals( PRODUCTS, template.getProducts() );
    }


    @Test
    public void testHashCodeNEquals() throws Exception
    {
        Map<Template, Template> map = new HashMap<>();

        map.put( template, template );

        assertEquals( TestUtils.getDefaultTemplate(), map.get( template ) );
        assertFalse( template.equals( null ) );
    }


    @Test
    public void testToString() throws Exception
    {

        assertTrue( template.toString().contains( TestUtils.TEMPLATE_NAME ) );
    }


    @Test
    public void testName() throws Exception
    {
        assertEquals( TestUtils.TEMPLATE_NAME, template.getTemplateName() );
    }


    @Test
    public void testArch() throws Exception
    {
        assertEquals( TestUtils.LXC_ARCH, template.getLxcArch() );
    }


    @Test
    public void testUtsName() throws Exception
    {
        assertEquals( TestUtils.UTS_NAME, template.getLxcUtsname() );
    }


    @Test
    public void testPackagesManifest() throws Exception
    {
        assertEquals( TestUtils.PACKAGES_MANIFEST, template.getPackagesManifest() );
    }


    @Test
    public void testConfigPath() throws Exception
    {
        assertEquals( TestUtils.CFG_PATH, template.getSubutaiConfigPath() );
    }


    @Test
    public void testGitBranch() throws Exception
    {
        assertEquals( TestUtils.GIT_BRANCH, template.getSubutaiGitBranch() );
    }


    @Test
    public void testGitId() throws Exception
    {
        assertEquals( TestUtils.GIT_UUID, template.getSubutaiGitUuid() );
    }


    @Test
    public void testSubutaiParent() throws Exception
    {
        assertEquals( TestUtils.SUBUTAI_PARENT, template.getSubutaiParent() );
    }


    @Test
    public void testAddChildren() throws Exception
    {
        template.addChildren( Lists.newArrayList( mock( Template.class ) ) );

        assertFalse( template.getChildren().isEmpty() );
    }


    @Test
    public void testRegistryException() throws Exception
    {
        RegistryException exception = new RegistryException( EXCEPTION_MSG );

        assertEquals( EXCEPTION_MSG, exception.getMessage() );
    }
}
