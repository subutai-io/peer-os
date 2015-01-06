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
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.safehaus.subutai.common.cache.ExpiringCache;
import org.safehaus.subutai.common.datatypes.TemplateVersion;
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

import org.apache.commons.lang3.tuple.Pair;

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
    private final ExpiringCache<String, Boolean> templateDownloadTokens = new ExpiringCache<>();


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
            String subutaiTemplateVersion = properties.getProperty( "subutai.template.package" );
            if ( subutaiTemplateVersion == null || "".equals( subutaiTemplateVersion ) )
            {
                subutaiTemplateVersion = Common.DEFAULT_TEMPLATE_VERSION;
            }
            else
            {
                String[] parsedVersion = subutaiTemplateVersion.split( "_" );
                subutaiTemplateVersion = parsedVersion[1];
            }

            LOG.warn( configFile );

            Template template = new Template( lxcArch, lxcUtsname, subutaiConfigPath, subutaiParent, subutaiGitBranch,
                    subutaiGitUuid, packagesFile, md5sum, new TemplateVersion( subutaiTemplateVersion ) );

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
        unregisterTemplate( templateName, new TemplateVersion( Common.DEFAULT_TEMPLATE_VERSION ), lxcArch );
    }


    /**
     * Removes template entry from registry
     *
     * @param templateName - name of template to remove
     * @param templateVersion - lxc architecture
     */
    public boolean unregisterTemplate( String templateName, TemplateVersion templateVersion, String lxcArch )
            throws RegistryException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateName ), TEMPLATE_IS_NULL_MSG );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcArch ), LXC_ARCH_IS_NULL_MSG );
        Preconditions.checkNotNull( templateVersion, "Template version is null." );

        //find template
        Template template = getTemplate( templateName, templateVersion, lxcArch );

        if ( template != null )
        {
            //check if template is used on FAIs

            if ( template.isInUseOnFAIs() )
            {
                throw new RegistryException( String.format( "Template %s is in use on %s", template.getTemplateName(),
                        template.getFaisUsingThisTemplate() ) );
            }

            //check if template has children
            List<Template> children = getChildTemplates( templateName, templateVersion, lxcArch );
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
        return true;
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
        return getTemplate( templateName, new TemplateVersion( Common.DEFAULT_TEMPLATE_VERSION ), lxcArch );
    }


    /**
     * Returns template by name and lxc arch
     *
     * @param templateName - name of template
     * @param templateVersion - template version
     *
     * @return {@code Template}
     */
    @Override
    public Template getTemplate( final String templateName, TemplateVersion templateVersion )
    {
        return getTemplate( templateName, new TemplateVersion( Common.DEFAULT_TEMPLATE_VERSION ),
                Common.DEFAULT_LXC_ARCH );
    }


    /**
     * Returns template by name and lxc arch
     *
     * @param templateName - name of template
     * @param lxcArch - lxc architecture
     * @param templateVersion - template version
     *
     * @return {@code Template}
     */
    @Override
    public Template getTemplate( final String templateName, TemplateVersion templateVersion, String lxcArch )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateName ), TEMPLATE_IS_NULL_MSG );
        Preconditions.checkNotNull( templateVersion, "Template version is null." );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcArch ), LXC_ARCH_IS_NULL_MSG );
        //retrieve template from storage
        try
        {
            return templateService.getTemplate( templateName, templateVersion, lxcArch );
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
    public Template getTemplate( final String templateName, String lxcArch, String md5sum,
                                 TemplateVersion templateVersion )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateName ), TEMPLATE_IS_NULL_MSG );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcArch ), LXC_ARCH_IS_NULL_MSG );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( md5sum ), "Template md5sum cannot be null." );
        Preconditions.checkNotNull( templateVersion, "Template version cannot be null." );
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
     *
     * @return {@code List<Template>}
     */
    @Override
    public List<Template> getChildTemplates( final String parentTemplateName, TemplateVersion templateVersion )
    {
        return getChildTemplates( parentTemplateName, templateVersion, Common.DEFAULT_LXC_ARCH );
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
        return getChildTemplates( parentTemplateName, new TemplateVersion( Common.DEFAULT_TEMPLATE_VERSION ), lxcArch );
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
    public List<Template> getChildTemplates( final String parentTemplateName, final TemplateVersion templateVersion,
                                             String lxcArch )
    {
        Preconditions
                .checkArgument( !Strings.isNullOrEmpty( parentTemplateName ), "Parent template name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcArch ), LXC_ARCH_IS_NULL_MSG );
        Preconditions.checkNotNull( templateVersion, "Template version is null." );
        //retrieve child templates from storage
        try
        {
            return templateService.getChildTemplates( parentTemplateName, templateVersion, lxcArch );
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
        return getParentTemplate( childTemplateName, new TemplateVersion( Common.DEFAULT_TEMPLATE_VERSION ), lxcArch );
    }


    /**
     * Returns parent template
     *
     * @param childTemplateName - child template name
     * @param templateVersion - child template version
     * @param lxcArch - lxc architecture
     *
     * @return {@code Template}
     */
    @Override
    public Template getParentTemplate( final String childTemplateName, final TemplateVersion templateVersion,
                                       final String lxcArch )
    {
        Preconditions
                .checkArgument( !Strings.isNullOrEmpty( childTemplateName ), "Child template name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcArch ), LXC_ARCH_IS_NULL_MSG );
        //retrieve parent template from storage
        Template child = getTemplate( childTemplateName, templateVersion, lxcArch );

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
     *
     * @return {@code List<Template>}
     */
    @Override
    public List<Template> getParentTemplates( String childTemplateName, TemplateVersion templateVersion )
    {
        return getParentTemplates( childTemplateName, templateVersion, Common.DEFAULT_LXC_ARCH );
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
        return getParentTemplates( childTemplateName, new TemplateVersion( Common.DEFAULT_TEMPLATE_VERSION ), lxcArch );
    }


    /**
     * Returns parent templates
     *
     * @param childTemplateName - name of template whose parents to return
     * @param templateVersion - version of template whose parents to return
     * @param lxcArch - lxc architecture
     *
     * @return {@code List<Template>}
     */
    @Override
    public List<Template> getParentTemplates( final String childTemplateName, final TemplateVersion templateVersion,
                                              final String lxcArch )
    {
        List<Template> parents = new ArrayList<>();

        Template parent = getParentTemplate( childTemplateName, templateVersion, lxcArch );
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
        return updateTemplateUsage( faiHostname, templateName, new TemplateVersion( Common.DEFAULT_TEMPLATE_VERSION ),
                inUse );
    }


    /**
     * Updates template usage on FAIs
     *
     * @param faiHostname - fai hostname
     * @param templateVersion - target template version
     * @param templateName - target template
     * @param inUse - true - template is in use, false - template is out of use
     */
    @Override
    public synchronized boolean updateTemplateUsage( final String faiHostname, final String templateName,
                                                     TemplateVersion templateVersion, final boolean inUse )
            throws RegistryException
    {

        Preconditions.checkArgument( !Strings.isNullOrEmpty( faiHostname ), "FAI hostname is null or empty" );
        Preconditions.checkNotNull( templateVersion, "Template version is null." );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateName ), TEMPLATE_IS_NULL_MSG );

        Template template = getTemplate( templateName, templateVersion );
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
        return isTemplateInUse( templateName, new TemplateVersion( Common.DEFAULT_TEMPLATE_VERSION ) );
    }


    /**
     * Indicates if template is in use on any of FAIs
     *
     * @param templateName - name of template
     *
     * @return true - in use, false - not in use
     */
    @Override
    public boolean isTemplateInUse( String templateName, TemplateVersion templateVersion ) throws RegistryException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateName ), TEMPLATE_IS_NULL_MSG );
        Preconditions.checkNotNull( templateVersion, "Template version is null." );

        Template template = getTemplate( templateName, templateVersion );
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
            LOG.warn( "Printing saved templates..." );
            List<Template> templates = templateService.getAllTemplates();
            for ( Template template1 : templates )
            {
                LOG.warn( template1.getTemplateName() );
            }
        }
        catch ( DaoException e )
        {
            LOG.error( "Error while saving template: ", e );
        }
    }


    public void dispose()
    {
        templateDownloadTokens.dispose();
    }


    @Override
    public String getTemplateDownloadToken( final int timeout )
    {
        Preconditions.checkArgument( timeout > 0, "Timeout must be greater than 0" );

        String templateDownloadToken = UUID.randomUUID().toString();
        templateDownloadTokens.put( templateDownloadToken, false, timeout * 1000 );
        return templateDownloadToken;
    }


    @Override
    public boolean checkTemplateDownloadToken( final String token )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( token ), "Invalid token" );

        return templateDownloadTokens.get( token ) != null;
    }


    @Override
    public Pair<String, String> getChangedFileVersions( String branchA, String branchB, GitChangedFile file )
    {
        try
        {
            final String aBranchVersion = gitManager.showFile( REPO_ROOT_PATH, branchA, file.getGitFilePath() );
            final String bBranchVersion = gitManager.showFile( REPO_ROOT_PATH, branchB, file.getGitFilePath() );
            return new Pair<String, String>()
            {
                @Override
                public String getLeft()
                {
                    return aBranchVersion;
                }


                @Override
                public String getRight()
                {
                    return bBranchVersion;
                }


                @Override
                public String setValue( final String value )
                {
                    throw new UnsupportedOperationException( "Cannot set value for object." );
                }
            };
        }
        catch ( GitException e )
        {
            LOG.error( "Error getting git file branch version.", e );
            return null;
        }
    }
}
