/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.registry.impl;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.safehaus.subutai.common.exception.DaoException;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.protocol.api.TemplateService;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.util.StringUtil;
import org.safehaus.subutai.core.git.api.GitChangedFile;
import org.safehaus.subutai.core.git.api.GitException;
import org.safehaus.subutai.core.git.api.GitManager;
import org.safehaus.subutai.core.registry.api.RegistryException;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;
import org.safehaus.subutai.core.registry.api.TemplateTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;


/**
 * This is an implementation of TemplateRegistryManager
 */
public class TemplateRegistryImpl implements TemplateRegistry
{

    private static final Logger LOG = LoggerFactory.getLogger( TemplateRegistryImpl.class.getName() );
    private static final String TEMPLATE_IS_NULL_MSG = "Template name is null or empty";
    private static final String LXC_ARCH_IS_NULL_MSG = "Lxc Arch is null or empty";
    private static final String TEMPLATE_NOT_FOUND_MSG = "Template %s not found";
    private static final String REPO_ROOT_PATH = "/var/lib/git/subutai.git/";


    public void setTemplateService( final TemplateService templateDAO )
    {
        this.templateService = templateDAO;
    }


    protected TemplateService templateService;

    private GitManager gitManager;


    public TemplateRegistryImpl() throws DaoException
    {
    }


    /**
     * Registers template in registry/database
     *
     * @param configFile - template configuration file contents
     * @param packagesFile - template packages manifest file contents
     * @param md5sum - template file's md5 hash
     */
    @Override
    public synchronized boolean registerTemplate( final String configFile, final String packagesFile,
                                                  final String md5sum ) throws RegistryException
    {

        Preconditions.checkArgument( !Strings.isNullOrEmpty( configFile ), "Config file contents is null or empty" );
        Preconditions
                .checkArgument( !Strings.isNullOrEmpty( packagesFile ), "Packages file contents is null or empty" );

        Template template = parseTemplate( configFile, packagesFile, md5sum );

        //save template to storage
        try
        {

            templateService.saveTemplate( template );
            return true;
        }
        catch ( Exception e )
        {
            LOG.error( "Error in registerTemplate", e );
            throw new RegistryException(
                    String.format( "Error saving template %s, %s", template.getTemplateName(), e.getMessage() ) );
        }
    }


    /**
     * Returns template parsing supplied  contents of config file , packages file and md5sum
     *
     * @param configFile - template configuration file contents
     * @param packagesFile - template packages manifest file contents
     * @param md5sum - template file's md5 hash
     *
     * @return {@code Template}
     */
    private Template parseTemplate( String configFile, String packagesFile, String md5sum ) throws RegistryException
    {
        Properties properties = new Properties();
        try
        {
            properties.load( new ByteArrayInputStream( configFile.getBytes( Charset.defaultCharset() ) ) );
            String lxcUtsname = properties.getProperty( "lxc.utsname" );
            String lxcArch = properties.getProperty( "lxc.arch" );
            String subutaiConfigPath = properties.getProperty( "subutai.config.path" );
            String subutaiParent = properties.getProperty( "subutai.parent" );
            String subutaiGitBranch = properties.getProperty( "subutai.git.branch" );
            String subutaiGitUuid = properties.getProperty( "subutai.git.uuid" );
            //TODO need to find out how to extract version from configFile
            String templateVersion = "2.1.0";

            LOG.warn( configFile );

            Template template = new Template( lxcArch, lxcUtsname, subutaiConfigPath, subutaiParent, subutaiGitBranch,
                    subutaiGitUuid, packagesFile, md5sum, templateVersion );

            //check if template with such name already exists
            if ( getTemplate( template.getTemplateName() ) != null )
            {
                throw new RegistryException(
                        String.format( "Template with name %s already exists", template.getTemplateName() ) );
            }

            //check if template with supplied md5sum already exists
            List<Template> allTemplates = getAllTemplates( template.getLxcArch() );
            for ( Template template1 : allTemplates )
            {
                if ( StringUtil.areStringsEqual( template1.getMd5sum(), template.getMd5sum() ) )
                {
                    throw new RegistryException( String.format( "Template %s with the same md5sum already exists",
                            template1.getTemplateName() ) );
                }
            }

            if ( template.getParentTemplateName() == null )
            {

                template.setProducts( getPackagesDiff( null, template ) );
            }
            else
            {

                Template parentTemplate = getTemplate( template.getParentTemplateName() );

                template.setProducts( getPackagesDiff( parentTemplate, template ) );
            }

            return template;
        }
        catch ( IOException | RuntimeException e )
        {
            LOG.error( "Error in parseTemplate", e );
            throw new RegistryException( String.format( "Error parsing template configuration %s", e ) );
        }
    }


    @Override
    public Set<String> getPackagesDiff( Template template )
    {
        if ( template.getParentTemplateName() == null )
        {
            return getPackagesDiff( null, template );
        }
        else
        {
            Template parentTemplate = getTemplate( template.getParentTemplateName() );
            return getPackagesDiff( parentTemplate, template );
        }
    }


    private Set<String> getPackagesDiff( Template parent, Template child )
    {
        if ( parent == null )
        {
            return extractPackageNames( Sets.newHashSet( child.getPackagesManifest().split( "\n" ) ) );
        }
        else
        {
            return getPackagesDiff( Sets.newHashSet( parent.getPackagesManifest().split( "\n" ) ),
                    Sets.newHashSet( child.getPackagesManifest().split( "\n" ) ) );
        }
    }


    private Set<String> extractPackageNames( Set<String> ls )
    {
        Pattern p = Pattern.compile( String.format( "(%s.+?)\\s", Common.PACKAGE_PREFIX ) );
        Matcher m = p.matcher( "" );

        SortedSet<String> res = new TreeSet<>();
        for ( String s : ls )
        {
            if ( m.reset( s ).find() )
            {
                res.add( m.group( 1 ) );
            }
        }

        return res;
    }


    private Set<String> getPackagesDiff( Set<String> parentPackages, Set<String> childPackages )
    {
        Set<String> p = extractPackageNames( parentPackages );
        Set<String> c = extractPackageNames( childPackages );
        c.removeAll( p );

        return c;
    }


    /**
     * Deletes template from registry
     *
     * @param templateName - name of template to remove
     */
    @Override
    public boolean unregisterTemplate( final String templateName ) throws RegistryException
    {
        unregisterTemplate( templateName, Common.DEFAULT_LXC_ARCH );
        return true;
    }


    /**
     * Deletes template from registry
     *
     * @param templateName - name of template to remove
     * @param lxcArch - lxc architecture
     */
    @Override
    public void unregisterTemplate( final String templateName, final String lxcArch ) throws RegistryException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateName ), TEMPLATE_IS_NULL_MSG );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcArch ), LXC_ARCH_IS_NULL_MSG );

        //find template
        Template template = getTemplate( templateName, lxcArch );

        if ( template != null )
        {
            //check if template is used on FAIs

            if ( template.isInUseOnFAIs() )
            {
                throw new RegistryException( String.format( "Template %s is in use on %s", template.getTemplateName(),
                        template.getFaisUsingThisTemplate() ) );
            }

            //check if template has children
            List<Template> children = getChildTemplates( templateName, lxcArch );
            if ( !children.isEmpty() )
            {
                throw new RegistryException(
                        String.format( "Can no delete template %s from registry because it has children",
                                templateName ) );
            }

            //delete template from storage
            try
            {
                templateService.removeTemplate( template );
            }
            catch ( Exception e )
            {
                LOG.error( "Error in unregisterTemplate", e );
                throw new RegistryException(
                        String.format( "Error deleting template %s, %s", templateName, e.getMessage() ) );
            }
        }
        else
        {
            throw new RegistryException( String.format( TEMPLATE_NOT_FOUND_MSG, templateName ) );
        }
    }


    /**
     * Returns template by name
     *
     * @param templateName - name of template
     *
     * @return {@code Template}
     */
    @Override
    public Template getTemplate( final String templateName )
    {
        return getTemplate( templateName, Common.DEFAULT_LXC_ARCH );
    }


    /**
     * Returns template by name and lxc arch
     *
     * @param templateName - name of template
     * @param lxcArch - lxc architecture
     *
     * @return {@code Template}
     */
    @Override
    public Template getTemplate( final String templateName, String lxcArch )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateName ), TEMPLATE_IS_NULL_MSG );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcArch ), LXC_ARCH_IS_NULL_MSG );
        //retrieve template from storage
        try
        {
            return templateService.getTemplate( templateName, lxcArch );
        }
        catch ( Exception e )
        {
            LOG.error( "Error in getTemplate", e );
            return null;
        }
    }


    /**
     * Returns template by name and lxc arch
     *
     * @param templateName - name of template
     * @param lxcArch - lxc architecture
     * @param md5sum - lxc md5sum
     * @param templateVersion - lxc templateVersion
     *
     * @return {@code Template}
     */
    @Override
    public Template getTemplate( final String templateName, String lxcArch, String md5sum, String templateVersion )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateName ), TEMPLATE_IS_NULL_MSG );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcArch ), LXC_ARCH_IS_NULL_MSG );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( md5sum ), "Template md5sum cannot be null." );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateVersion ), "Template version cannot be null." );
        //retrieve template from storage
        try
        {
            return templateService.getTemplate( templateName, lxcArch, md5sum, templateVersion );
        }
        catch ( Exception e )
        {
            LOG.error( "Error in getTemplate", e );
            return null;
        }
    }


    /**
     * Returns child templates
     *
     * @param parentTemplateName - parent template name
     *
     * @return {@code List<Template>}
     */
    @Override
    public List<Template> getChildTemplates( final String parentTemplateName )
    {
        return getChildTemplates( parentTemplateName, Common.DEFAULT_LXC_ARCH );
    }


    /**
     * Returns child templates
     *
     * @param parentTemplateName - parent template name
     * @param lxcArch - lxc architecture
     *
     * @return {@code List<Template>}
     */
    @Override
    public List<Template> getChildTemplates( final String parentTemplateName, String lxcArch )
    {
        Preconditions
                .checkArgument( !Strings.isNullOrEmpty( parentTemplateName ), "Parent template name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcArch ), LXC_ARCH_IS_NULL_MSG );
        //retrieve child templates from storage
        try
        {
            return templateService.getChildTemplates( parentTemplateName, lxcArch );
        }
        catch ( Exception e )
        {
            LOG.error( "Error in getChildTemplates", e );
            return Collections.emptyList();
        }
    }


    /**
     * Returns parent template
     *
     * @param childTemplateName - child template name
     *
     * @return {@code Template}
     */
    @Override
    public Template getParentTemplate( final String childTemplateName )
    {
        return getParentTemplate( childTemplateName, Common.DEFAULT_LXC_ARCH );
    }


    /**
     * Returns parent template
     *
     * @param childTemplateName - child template name
     * @param lxcArch - lxc architecture
     *
     * @return {@code Template}
     */
    @Override
    public Template getParentTemplate( final String childTemplateName, final String lxcArch )
    {
        Preconditions
                .checkArgument( !Strings.isNullOrEmpty( childTemplateName ), "Child template name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcArch ), LXC_ARCH_IS_NULL_MSG );
        //retrieve parent template from storage
        Template child = getTemplate( childTemplateName, lxcArch );

        if ( child != null && !Strings.isNullOrEmpty( child.getParentTemplateName() ) )
        {
            return getTemplate( child.getParentTemplateName(), child.getLxcArch() );
        }

        return null;
    }


    /**
     * Returns template tree
     *
     * @return {@code TemplateTree}
     */
    @Override
    public TemplateTree getTemplateTree()
    {
        //retrieve all templates and fill template tree
        TemplateTree templateTree = new TemplateTree();
        try
        {
            List<Template> allTemplates = templateService.getAllTemplates();
            for ( Template template : allTemplates )
            {
                templateTree.addTemplate( template );
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Error in getTemplateTree", e );
        }
        return templateTree;
    }


    /**
     * Returns parent templates
     *
     * @param childTemplateName - name of template whose parents to return
     *
     * @return {@code List<Template>}
     */
    @Override
    public List<Template> getParentTemplates( String childTemplateName )
    {
        return getParentTemplates( childTemplateName, Common.DEFAULT_LXC_ARCH );
    }


    /**
     * Returns parent templates
     *
     * @param childTemplateName - name of template whose parents to return
     * @param lxcArch - lxc architecture
     *
     * @return {@code List<Template>}
     */
    @Override
    public List<Template> getParentTemplates( final String childTemplateName, final String lxcArch )
    {
        List<Template> parents = new ArrayList<>();

        Template parent = getParentTemplate( childTemplateName, lxcArch );
        while ( parent != null )
        {
            parents.add( parent );
            parent = getParentTemplate( parent.getTemplateName(), lxcArch );
        }
        Collections.reverse( parents );
        return parents;
    }


    /**
     * Returns all templates
     *
     * @return {@code List<Template>}
     */
    @Override
    public List<Template> getAllTemplates()
    {
        return getAllTemplates( Common.DEFAULT_LXC_ARCH );
    }


    /**
     * Returns all templates
     *
     * @param lxcArch - lxc architecture
     *
     * @return {@code List<Template>}
     */
    @Override
    public List<Template> getAllTemplates( final String lxcArch )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcArch ), LXC_ARCH_IS_NULL_MSG );

        try
        {
            List<Template> allTemplates = templateService.getAllTemplates();
            List<Template> result = new ArrayList<>();
            for ( Template template : allTemplates )
            {
                if ( template.getLxcArch().equalsIgnoreCase( lxcArch ) )
                {
                    result.add( template );
                }
            }
            return result;
        }
        catch ( Exception e )
        {
            LOG.error( "Error in getAllTemplates", e );
        }

        return Collections.emptyList();
    }


    /**
     * Updates template usage on FAIs
     *
     * @param faiHostname - fai hostname
     * @param templateName - target template
     * @param inUse - true - template is in use, false - template is out of use
     */
    @Override
    public synchronized boolean updateTemplateUsage( final String faiHostname, final String templateName,
                                                     final boolean inUse ) throws RegistryException
    {

        Preconditions.checkArgument( !Strings.isNullOrEmpty( faiHostname ), "FAI hostname is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateName ), TEMPLATE_IS_NULL_MSG );

        Template template = getTemplate( templateName );
        if ( template == null )
        {
            throw new RegistryException( String.format( TEMPLATE_NOT_FOUND_MSG, templateName ) );
        }
        else
        {
            template.setInUseOnFAI( faiHostname, inUse );
            try
            {
                templateService.saveTemplate( template );
            }
            catch ( Exception e )
            {
                LOG.error( "Error in updateTemplateUsage", e );
                throw new RegistryException( String.format( "Error saving template information, %s", e.getMessage() ) );
            }
        }
        return true;
    }


    /**
     * Indicates if template is in use on any of FAIs
     *
     * @param templateName - name of template
     *
     * @return true - in use, false - not in use
     */
    @Override
    public boolean isTemplateInUse( String templateName ) throws RegistryException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateName ), TEMPLATE_IS_NULL_MSG );

        Template template = getTemplate( templateName );
        if ( template == null )
        {
            throw new RegistryException( String.format( TEMPLATE_NOT_FOUND_MSG, templateName ) );
        }
        else
        {
            return template.isInUseOnFAIs();
        }
    }


    @Override
    public boolean registerTemplate( Template template ) throws RegistryException
    {
        //save template to storage
        try
        {

            templateService.saveTemplate( template );
        }
        catch ( Exception e )
        {
            LOG.error( "Error in registerTemplate", e );
            throw new RegistryException(
                    String.format( "Error saving template %s, %s", template.getTemplateName(), e.getMessage() ) );
        }
        return true;
    }


    public List<GitChangedFile> getChangedFiles( Template template ) throws RegistryException
    {
        String parentBranch = template.getParentTemplateName();
        String templateBranch = template.getTemplateName();
        if ( parentBranch == null || "".equals( parentBranch ) )
        {
            parentBranch = templateBranch;
        }
        return getChangedFiles( parentBranch, templateBranch );
    }


    private List<GitChangedFile> getChangedFiles( String parentBranch, String childBranch ) throws RegistryException
    {
        try
        {
            return getGitManager().diffBranches( REPO_ROOT_PATH, parentBranch, childBranch );
        }
        catch ( GitException e )
        {
            return Collections.emptyList();
        }
    }


    public GitManager getGitManager()
    {
        return gitManager;
    }


    public void setGitManager( final GitManager gitManager )
    {
        this.gitManager = gitManager;
    }


    public void init()
    {
        try
        {
            //                        Template template = TestUtilsDuplicate.getParentTemplate();
            //                        templateService.saveTemplate( template );

            LOG.warn( "Printing saved templates..." );
            List<Template> templates = templateService.getAllTemplates();
            for ( Template template1 : templates )
            {
                LOG.warn( template1.getTemplateName() );
            }
        }
        //        catch ( IOException e )
        //        {
        //            LOG.error( "Error in TestUtilsDuplicate.getParentTemplate()@TemplateRegistryImpl service: ", e );
        //        }
        catch ( DaoException e )
        {
            LOG.error( "Error while saving template: ", e );
        }
    }
}
