package org.safehaus.subutai.common.protocol;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlRootElement;

import org.safehaus.subutai.common.datatypes.TemplateVersion;
import org.safehaus.subutai.common.settings.Common;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.annotations.Expose;


/**
 * Template represents template entry in registry
 */
@Entity( name = "Template" )
@NamedQueries( value = {
        @NamedQuery( name = "Template.getAll", query = "SELECT t FROM Template t" ),
        @NamedQuery( name = "Template.getTemplateByNameArch",
                query = "SELECT t FROM Template t WHERE t.pk.templateName = :templateName AND t.pk.lxcArch = "
                        + ":lxcArch" ), @NamedQuery( name = "Template.getTemplateByNameArchMd5Version",
        query = "SELECT t FROM Template t WHERE t.pk.templateName = :templateName AND t.pk.lxcArch = "
                + ":lxcArch AND t.pk.templateVersion = :templateVersion AND t.pk.md5sum = :md5sum" ),
        @NamedQuery( name = "Template.removeTemplateByNameArch",
                query = "DELETE FROM Template t WHERE t.pk.templateName = :templateName AND t.pk.lxcArch = :lxcArch" )
} )
@XmlRootElement( name = "" )
public class Template
{
    public static final String ARCH_AMD64 = "amd64";
    public static final String ARCH_I386 = "i386";

    public static final String QUERY_GET_ALL = "Template.getAll";
    public static final String QUERY_GET_TEMPLATE_BY_NAME_ARCH = "Template.getTemplateByNameArch";
    public static final String QUERY_GET_TEMPLATE_BY_NAME_ARCH_MD5_VERSION = "Template.getTemplateByNameArchMd5Version";
    public static final String QUERY_REMOVE_TEMPLATE_BY_NAME_ARCH = "Template.removeTemplateByNameArch";

    @Expose
    @EmbeddedId
    TemplatePK pk;


    //name of parent template
    @Expose
    private String parentTemplateName;

    //lxc container name
    @Expose
    private String lxcUtsname;

    //path to cfg files tracked by subutai
    @Expose
    private String subutaiConfigPath;

    //name of parent template
    @Expose
    private String subutaiParent;

    //name of git branch where template cfg files are versioned
    @Expose
    private String subutaiGitBranch;

    //id of git commit which pushed template cfg files to git
    @Expose
    private String subutaiGitUuid;

    //contents of packages manifest file
    @Lob
    private String packagesManifest;

    //children of template, this property is calculated upon need and is null by default (see REST API for calculation)
    @Expose
    @OneToMany( fetch = FetchType.EAGER, cascade = { CascadeType.ALL }, orphanRemoval = true )
    private Set<Template> children = new HashSet<>();

    //subutai products present only in this template excluding all subutai products present in the whole ancestry
    // lineage above
    @Expose
    @ElementCollection( targetClass = String.class, fetch = FetchType.EAGER )
    private Set<String> products = new HashSet<>();


    //indicates whether this template is in use on any of FAIs connected to Subutai
    @Expose
    @ElementCollection( targetClass = String.class, fetch = FetchType.EAGER )
    private Set<String> faisUsingThisTemplate = new HashSet<>();

    //indicates where template is generated
    @Expose
    private UUID peerId;

    @Expose
    private boolean remote = false;


    public Template()
    {
    }


    /**
     * @param lxcArch - lxcArch
     * @param lxcUtsname - lxcUtsname
     * @param subutaiConfigPath - subutaiConfigPath
     * @param subutaiParent - subutaiParent
     * @param subutaiGitBranch - subutaiGitBranch
     * @param subutaiGitUuid - subutaiGitUuid
     * @param packagesManifest - packagesManifest
     * @param md5sum - md5sum
     *
     * @deprecated (since versioning was introduced, pass additional template version)
     */
    @Deprecated
    public Template( final String lxcArch, final String lxcUtsname, final String subutaiConfigPath,
                     final String subutaiParent, final String subutaiGitBranch, final String subutaiGitUuid,
                     final String packagesManifest, final String md5sum )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcUtsname ), "Missing lxc.utsname parameter" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcArch ), "Missing lxc.arch parameter" );
        Preconditions
                .checkArgument( !Strings.isNullOrEmpty( subutaiConfigPath ), "Missing subutai.config.path parameter" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( subutaiParent ), "Missing subutai.parent parameter" );
        Preconditions
                .checkArgument( !Strings.isNullOrEmpty( subutaiGitBranch ), "Missing subutai.git.branch parameter" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( subutaiGitUuid ), "Missing subutai.git.uuid parameter" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( packagesManifest ), "Missing packages manifest" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( md5sum ), "Missing md5sum" );

        this.pk = new TemplatePK( lxcUtsname, lxcArch, new TemplateVersion( Common.DEFAULT_TEMPLATE_VERSION ), md5sum );
        this.lxcUtsname = lxcUtsname;
        this.subutaiConfigPath = subutaiConfigPath;
        this.subutaiParent = subutaiParent;
        this.subutaiGitBranch = subutaiGitBranch;
        this.subutaiGitUuid = subutaiGitUuid;
        this.packagesManifest = packagesManifest;
        this.parentTemplateName = subutaiParent;

        if ( this.pk.getTemplateName().equalsIgnoreCase( parentTemplateName ) )
        {
            parentTemplateName = null;
        }
    }


    public Template( final String lxcArch, final String lxcUtsname, final String subutaiConfigPath,
                     final String subutaiParent, final String subutaiGitBranch, final String subutaiGitUuid,
                     final String packagesManifest, final String md5sum, final TemplateVersion templateVersion )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcUtsname ), "Missing lxc.utsname parameter" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcArch ), "Missing lxc.arch parameter" );
        Preconditions
                .checkArgument( !Strings.isNullOrEmpty( subutaiConfigPath ), "Missing subutai.config.path parameter" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( subutaiParent ), "Missing subutai.parent parameter" );
        Preconditions
                .checkArgument( !Strings.isNullOrEmpty( subutaiGitBranch ), "Missing subutai.git.branch parameter" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( subutaiGitUuid ), "Missing subutai.git.uuid parameter" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( packagesManifest ), "Missing packages manifest" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( md5sum ), "Missing md5sum" );
        Preconditions.checkNotNull( templateVersion, "Missing templateVersion" );

        this.pk = new TemplatePK( lxcUtsname, lxcArch, templateVersion, md5sum );
        this.lxcUtsname = lxcUtsname;
        this.subutaiConfigPath = subutaiConfigPath;
        this.subutaiParent = subutaiParent;
        this.subutaiGitBranch = subutaiGitBranch;
        this.subutaiGitUuid = subutaiGitUuid;
        this.parentTemplateName = subutaiParent;
        this.packagesManifest = packagesManifest;


        if ( this.pk.getTemplateName().equalsIgnoreCase( parentTemplateName ) )
        {
            parentTemplateName = null;
        }
    }


    public TemplatePK getPk()
    {
        return pk;
    }


    public void setPk( final TemplatePK pk )
    {
        this.pk = pk;
    }


    public boolean isInUseOnFAIs()
    {
        return faisUsingThisTemplate != null && !faisUsingThisTemplate.isEmpty();
    }


    public void setInUseOnFAI( final String faiHostname, final boolean inUseOnFAI )
    {
        if ( faisUsingThisTemplate == null )
        {
            faisUsingThisTemplate = new HashSet<>();
        }
        if ( inUseOnFAI )
        {
            faisUsingThisTemplate.add( faiHostname );
        }
        else
        {
            faisUsingThisTemplate.remove( faiHostname );
        }
    }


    public Set<String> getFaisUsingThisTemplate()
    {
        return Collections.unmodifiableSet( faisUsingThisTemplate );
    }


    public void addChildren( List<Template> children )
    {
        if ( this.children == null )
        {
            this.children = new HashSet<>();
        }
        this.children.addAll( children );
    }


    public List<Template> getChildren()
    {
        if ( children != null )
        {
            return Collections.unmodifiableList( new ArrayList<>( children ) );
        }
        else
        {
            return Collections.emptyList();
        }
    }


    public String getMd5sum()
    {
        return pk.getMd5sum();
    }


    public Set<String> getProducts()
    {
        return products;
    }


    public void setProducts( final Set<String> products )
    {
        this.products = products;
    }


    public String getLxcArch()
    {
        return this.pk.getLxcArch();
    }


    public String getLxcUtsname()
    {
        return lxcUtsname;
    }


    public String getSubutaiConfigPath()
    {
        return subutaiConfigPath;
    }


    public String getSubutaiParent()
    {
        return subutaiParent;
    }


    public String getSubutaiGitBranch()
    {
        return subutaiGitBranch;
    }


    public String getSubutaiGitUuid()
    {
        return subutaiGitUuid;
    }


    public String getPackagesManifest()
    {
        return packagesManifest;
    }


    public String getTemplateName()
    {
        return this.pk.getTemplateName();
    }


    public String getFileName()
    {
        return String.format( "%s-subutai-template_%s_%s.deb", pk.getTemplateName(), pk.getTemplateVersion(),
                pk.getLxcArch() ).toLowerCase();
    }


    public String getParentTemplateName()
    {
        return parentTemplateName;
    }


    public TemplateVersion getTemplateVersion()
    {
        return pk.getTemplateVersion();
    }


    public UUID getPeerId()
    {
        return peerId;
    }


    private void setPeerId( final UUID peerId )
    {
        this.peerId = peerId;
    }


    public boolean isRemote()
    {
        return remote;
    }


    private void setRemote( final boolean remote )
    {
        this.remote = remote;
    }


    public Template getRemoteClone( UUID peerId )
    {
        Template result =
                new Template( this.pk.getLxcArch(), this.lxcUtsname, this.subutaiConfigPath, this.subutaiParent,
                        this.subutaiGitBranch, this.subutaiGitUuid, this.packagesManifest, this.pk.getMd5sum(),
                        this.pk.getTemplateVersion() );
        result.setRemote( true );
        result.setPeerId( peerId );
        return result;
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof Template ) )
        {
            return false;
        }

        final Template template = ( Template ) o;

        return pk.equals( template.pk );
    }


    @Override
    public int hashCode()
    {
        return pk.hashCode();
    }


    @Override
    public String toString()
    {
        return "Template{" +
                "templateName='" + pk.getTemplateName() + '\'' +
                ", parentTemplateName='" + parentTemplateName + '\'' +
                ", lxcArch='" + pk.getLxcArch() + '\'' +
                ", lxcUtsname='" + lxcUtsname + '\'' +
                ", subutaiConfigPath='" + subutaiConfigPath + '\'' +
                ", subutaiParent='" + subutaiParent + '\'' +
                ", subutaiGitBranch='" + subutaiGitBranch + '\'' +
                ", subutaiGitUuid='" + subutaiGitUuid + '\'' +
                ", children=" + children +
                ", products=" + products +
                ", md5sum='" + pk.getMd5sum() + '\'' +
                ", templateVersion='" + pk.getTemplateVersion() + '\'' +
                ", faisUsingThisTemplate=" + faisUsingThisTemplate +
                ", peerId=" + peerId +
                ", remote=" + remote +
                '}';
    }
}
