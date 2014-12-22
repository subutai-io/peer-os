package org.safehaus.subutai.core.registry.impl;


import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.safehaus.subutai.common.datatypes.TemplateVersion;
import org.safehaus.subutai.common.exception.DaoException;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.protocol.api.TemplateService;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.registry.api.RegistryException;
import org.safehaus.subutai.core.registry.api.TemplateTree;

import com.google.common.collect.Lists;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test for TemplateRegistryImpl
 */
public class TemplateRegistryImplTest
{

    private TemplateRegistryImpl templateRegistry;
    private TemplateService templateService;


    @Before
    public void setUp() throws Exception
    {
        templateRegistry = new TemplateRegistryImpl();
        templateService = mock( TemplateService.class );
        templateRegistry.setTemplateService( templateService );
    }


    @Test
    public void testRegisterTemplate() throws Exception
    {
        templateRegistry.registerTemplate( TestUtils.CONFIG_FILE, TestUtils.PACKAGES_MANIFEST, TestUtils.MD_5_SUM );

        verify( templateService ).saveTemplate( TestUtils.getParentTemplate() );
    }


    @Test( expected = RegistryException.class )
    public void testRegisterTemplateRuntimeException() throws Exception
    {
        Mockito.doThrow( new RuntimeException() ).when( templateService ).saveTemplate( TestUtils.getParentTemplate() );


        templateRegistry.registerTemplate( TestUtils.CONFIG_FILE, TestUtils.PACKAGES_MANIFEST, TestUtils.MD_5_SUM );
    }


    @Test
    public void testRegisterChildTemplate() throws Exception
    {
        when( templateService.getTemplate( TestUtils.TEMPLATE_NAME, TestUtils.LXC_ARCH ) )
                .thenReturn( TestUtils.getParentTemplate() );

        templateRegistry
                .registerTemplate( TestUtils.CHILD_CONFIG_FILE, TestUtils.CHILD_PACKAGES_MANIFEST, TestUtils.MD_5_SUM );

        ArgumentCaptor<Template> templateArgumentCaptor = ArgumentCaptor.forClass( Template.class );

        verify( templateService ).saveTemplate( templateArgumentCaptor.capture() );

        assertTrue( templateArgumentCaptor.getValue().getProducts().contains( TestUtils.CHILD_PACKAGE ) );
    }


    @Test( expected = RegistryException.class )
    public void testRegisterTemplateDuplicate() throws Exception
    {
        when( templateService.getTemplate( TestUtils.TEMPLATE_NAME, new TemplateVersion( Common.DEFAULT_TEMPLATE_VERSION ), TestUtils.LXC_ARCH ) )
                .thenReturn( TestUtils.getParentTemplate() );

        templateRegistry.registerTemplate( TestUtils.CONFIG_FILE, TestUtils.PACKAGES_MANIFEST, TestUtils.MD_5_SUM );
    }


    @Test( expected = RegistryException.class )
    public void testRegisterTemplateDuplicateByMd5Sum() throws Exception
    {
        List<Template> allTemplates = Lists.newArrayList( TestUtils.getParentTemplate() );
        when( templateService.getAllTemplates() ).thenReturn( allTemplates );

        templateRegistry.registerTemplate( TestUtils.CONFIG_FILE, TestUtils.PACKAGES_MANIFEST, TestUtils.MD_5_SUM );
    }


    @Test( expected = RegistryException.class )
    public void testRegisterTemplateException() throws Exception
    {
        Mockito.doThrow( new DaoException( "" ) ).when( templateService ).saveTemplate( any( Template.class ) );

        templateRegistry.registerTemplate( TestUtils.CONFIG_FILE, TestUtils.PACKAGES_MANIFEST, TestUtils.MD_5_SUM );
    }


    @Test
    public void testGetPackagesDiff() throws Exception
    {
        Set<String> diff = templateRegistry.getPackagesDiff( TestUtils.getParentTemplate() );

        assertTrue( diff.contains( TestUtils.PARENT_PACKAGE ) );
    }


    @Test
    public void testGetPackagesDiffWithChild() throws Exception
    {
        when( templateService.getTemplate( TestUtils.TEMPLATE_NAME, TestUtils.LXC_ARCH ) )
                .thenReturn( TestUtils.getParentTemplate() );

        Set<String> diff = templateRegistry.getPackagesDiff( TestUtils.getChildTemplate() );

        assertTrue( diff.contains( TestUtils.CHILD_PACKAGE ) );
    }


    @Test
    public void testUnregisterTemplate() throws Exception
    {
        when( templateService.getTemplate( TestUtils.TEMPLATE_NAME,new TemplateVersion( Common.DEFAULT_TEMPLATE_VERSION ), TestUtils.LXC_ARCH ) )
                .thenReturn( TestUtils.getParentTemplate() );


        templateRegistry.unregisterTemplate( TestUtils.TEMPLATE_NAME );


        verify( templateService ).removeTemplate( TestUtils.getParentTemplate() );
    }


    @Test( expected = RegistryException.class )
    public void testUnregisterTemplateException() throws Exception
    {
        when( templateService.getTemplate( TestUtils.TEMPLATE_NAME, TestUtils.LXC_ARCH ) )
                .thenReturn( TestUtils.getParentTemplate() );
        Mockito.doThrow( new DaoException( "" ) ).when( templateService ).removeTemplate( any( Template.class ) );

        templateRegistry.unregisterTemplate( TestUtils.TEMPLATE_NAME );
    }


    @Test( expected = RegistryException.class )
    public void testUnregisterTemplateException2() throws Exception
    {
        templateRegistry.unregisterTemplate( TestUtils.TEMPLATE_NAME );
    }


    @Test( expected = RegistryException.class )
    public void testUnregisterTemplateException3() throws Exception
    {
        Template template = TestUtils.getParentTemplate();
        template.setInUseOnFAI( "hostname", true );
        when( templateService.getTemplate( TestUtils.TEMPLATE_NAME, TestUtils.LXC_ARCH ) ).thenReturn( template );

        templateRegistry.unregisterTemplate( TestUtils.TEMPLATE_NAME );
    }


    @Test( expected = RegistryException.class )
    public void testUnregisterTemplateException4() throws Exception
    {
        when( templateService.getTemplate( TestUtils.TEMPLATE_NAME, TestUtils.LXC_ARCH ) )
                .thenReturn( TestUtils.getParentTemplate() );
        when( templateService.getChildTemplates( TestUtils.TEMPLATE_NAME, TestUtils.LXC_ARCH ) )
                .thenReturn( Lists.newArrayList( TestUtils.getChildTemplate() ) );

        templateRegistry.unregisterTemplate( TestUtils.TEMPLATE_NAME );
    }


    @Test
    public void testGetTemplate() throws Exception
    {
        when( templateService.getTemplate( TestUtils.TEMPLATE_NAME, new TemplateVersion( Common.DEFAULT_TEMPLATE_VERSION ), TestUtils.LXC_ARCH ) )
                .thenReturn( TestUtils.getParentTemplate() );

        Template template = templateRegistry.getTemplate( TestUtils.TEMPLATE_NAME );

        assertEquals( TestUtils.getParentTemplate(), template );
    }


    @Test
    public void testGetTemplateException() throws Exception
    {
        Mockito.doThrow( new DaoException( "" ) ).when( templateService )
               .getTemplate( TestUtils.TEMPLATE_NAME, TestUtils.LXC_ARCH );

        assertNull( templateRegistry.getTemplate( TestUtils.TEMPLATE_NAME ) );
    }


    @Test
    public void testGetChildTemplates() throws Exception
    {
        when( templateService.getChildTemplates( TestUtils.TEMPLATE_NAME, new TemplateVersion( Common.DEFAULT_TEMPLATE_VERSION ), TestUtils.LXC_ARCH ) )
                .thenReturn( Lists.newArrayList( TestUtils.getChildTemplate() ) );

        List<Template> children = templateRegistry.getChildTemplates( TestUtils.TEMPLATE_NAME );

        assertTrue( children.contains( TestUtils.getChildTemplate() ) );
    }


    @Test
    public void testGetChildTemplatesException() throws Exception
    {
        Mockito.doThrow( new DaoException( "" ) ).when( templateService )
               .getChildTemplates( TestUtils.TEMPLATE_NAME, TestUtils.LXC_ARCH );

        List<Template> children = templateRegistry.getChildTemplates( TestUtils.TEMPLATE_NAME );

        assertTrue( children.isEmpty() );
    }


    @Test
    public void testGetParentTemplate() throws Exception
    {
        when( templateService.getTemplate( TestUtils.TEMPLATE_NAME, new TemplateVersion( Common.DEFAULT_TEMPLATE_VERSION ), TestUtils.LXC_ARCH ) )
                .thenReturn( TestUtils.getParentTemplate() );
        when( templateService.getTemplate( TestUtils.CHILD_TEMPLATE_NAME, new TemplateVersion( Common.DEFAULT_TEMPLATE_VERSION ), TestUtils.LXC_ARCH ) )
                .thenReturn( TestUtils.getChildTemplate() );

        Template template = templateRegistry.getParentTemplate( TestUtils.CHILD_TEMPLATE_NAME );

        assertEquals( TestUtils.getParentTemplate(), template );
    }


    @Test
    public void testGetParentTemplateNull() throws Exception
    {
        Template template = templateRegistry.getParentTemplate( TestUtils.CHILD_TEMPLATE_NAME );

        assertNull( template );
    }


    @Test
    public void testGetTemplateTree() throws Exception
    {
        List<Template> allTemplates = Lists.newArrayList( TestUtils.getParentTemplate(), TestUtils.getChildTemplate() );
        when( templateService.getAllTemplates() ).thenReturn( allTemplates );

        TemplateTree templateTree = templateRegistry.getTemplateTree();

        assertTrue( templateTree.getChildrenTemplates( TestUtils.getParentTemplate() )
                                .contains( TestUtils.getChildTemplate() ) );
        assertTrue( templateTree.getRootTemplates().contains( TestUtils.getParentTemplate() ) );
        assertEquals( TestUtils.getParentTemplate(), templateTree.getParentTemplate( TestUtils.getChildTemplate() ) );
    }


    @Test
    public void testGetTemplateTreeException() throws Exception
    {
        when( templateService.getAllTemplates() ).thenReturn( Collections.<Template>emptyList() );

        TemplateTree templateTree = templateRegistry.getTemplateTree();

        assertNull( templateTree.getRootTemplates() );
    }


    @Test
    public void testGetParentTemplates() throws Exception
    {
        when( templateService.getTemplate( TestUtils.TEMPLATE_NAME, new TemplateVersion( Common.DEFAULT_TEMPLATE_VERSION ), TestUtils.LXC_ARCH ) )
                .thenReturn( TestUtils.getParentTemplate() );
        when( templateService.getTemplate( TestUtils.CHILD_TEMPLATE_NAME, new TemplateVersion( Common.DEFAULT_TEMPLATE_VERSION ), TestUtils.LXC_ARCH ) )
                .thenReturn( TestUtils.getChildTemplate() );

        List<Template> templates = templateRegistry.getParentTemplates( TestUtils.CHILD_TEMPLATE_NAME );

        assertTrue( templates.contains( TestUtils.getParentTemplate() ) );
        assertEquals( 1, templates.size() );
    }


    @Test
    public void testGetAllTemplates() throws Exception
    {
        List<Template> allTemplates = Lists.newArrayList( TestUtils.getParentTemplate(), TestUtils.getChildTemplate() );
        when( templateService.getAllTemplates() ).thenReturn( allTemplates );

        List<Template> templates = templateRegistry.getAllTemplates();

        assertEquals( allTemplates, templates );
    }


    @Test
    public void testGetAllTemplatesException() throws Exception
    {
        when( templateService.getAllTemplates() ).thenReturn( Collections.<Template>emptyList() );

        List<Template> templates = templateRegistry.getAllTemplates();

        assertTrue( templates.isEmpty() );
    }


    @Test
    public void testUpdateTemplateUsage() throws Exception
    {
        Template template = TestUtils.getParentTemplate();
        when( templateService.getTemplate( TestUtils.TEMPLATE_NAME, new TemplateVersion( Common.DEFAULT_TEMPLATE_VERSION ), TestUtils.LXC_ARCH ) ).thenReturn(
                template );

        templateRegistry.updateTemplateUsage( "hostname", TestUtils.TEMPLATE_NAME, true );

        assertTrue( template.isInUseOnFAIs() );

        templateRegistry.updateTemplateUsage( "hostname", TestUtils.TEMPLATE_NAME, false );

        assertFalse( template.isInUseOnFAIs() );

        verify( templateService, times( 2 ) ).saveTemplate( template );
    }


    @Test( expected = RegistryException.class )
    public void testUpdateTemplateUsageException2() throws Exception
    {
        Template template = TestUtils.getParentTemplate();
        when( templateService.getTemplate( TestUtils.TEMPLATE_NAME, TestUtils.LXC_ARCH ) ).thenReturn( template );
        Mockito.doThrow( new DaoException( "" ) ).when( templateService ).saveTemplate( template );

        templateRegistry.updateTemplateUsage( "hostname", TestUtils.TEMPLATE_NAME, true );
    }


    @Test( expected = RegistryException.class )
    public void testUpdateTemplateUsageException() throws Exception
    {
        templateRegistry.updateTemplateUsage( "hostname", TestUtils.TEMPLATE_NAME, true );
    }


    @Test
    public void testIsTemplateInUse() throws Exception
    {
        Template template = TestUtils.getParentTemplate();
        when( templateService.getTemplate( TestUtils.TEMPLATE_NAME, new TemplateVersion( Common.DEFAULT_TEMPLATE_VERSION ), TestUtils.LXC_ARCH ) ).thenReturn( template );

        template.setInUseOnFAI( "hostname", true );

        assertTrue( templateRegistry.isTemplateInUse( TestUtils.TEMPLATE_NAME ) );

        template.setInUseOnFAI( "hostname", false );

        assertFalse( templateRegistry.isTemplateInUse( TestUtils.TEMPLATE_NAME ) );
    }


    @Test( expected = RegistryException.class )
    public void testIsTemplateInUseException() throws Exception
    {
        templateRegistry.isTemplateInUse( TestUtils.TEMPLATE_NAME );
    }


    @Test
    public void testRegisterTemplate2() throws Exception
    {
        templateRegistry.registerTemplate( TestUtils.getChildTemplate() );

        verify( templateService ).saveTemplate( TestUtils.getChildTemplate() );
    }


    @Test( expected = RegistryException.class )
    public void testRegisterTemplate2Exception() throws Exception
    {
        Mockito.doThrow( new DaoException( "" ) ).when( templateService )
               .saveTemplate( any(Template.class) );

        templateRegistry.registerTemplate( TestUtils.getChildTemplate() );
    }
}
