package org.safehaus.subutai.common.protocol;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.annotations.Expose;


/**
 * Template represents template entry in registry
 */
@Entity( name = "Template" )
@IdClass( TemplatePK.class )
//@Table(name = "Template")
@NamedQueries( value = {
        @NamedQuery( name = "Template.getAll", query = "SELECT t FROM Template t" ),
        @NamedQuery( name = "Template.getTemplateByNameArch",
                query = "SELECT t FROM Template t WHERE t.templateName = :templateName AND t.lxcArch = " + ":lxcArch" ),
        @NamedQuery( name = "Template.getTemplateByNameArchMd5Version",
                query = "SELECT t FROM Template t WHERE t.templateName = :templateName AND t.lxcArch = "
                        + ":lxcArch AND t.templateVersion = :templateVersion AND t.md5sum = :md5sum" ),
        @NamedQuery( name = "Template.removeTemplateByNameArch",
                query = "DELETE FROM Template t WHERE t.templateName = :templateName AND t.lxcArch = :lxcArch" )
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

    //    @EmbeddedId
    //    TemplatePK pk;

    @Id
    @Expose
    private String templateName;


    @Id
    @Expose
    private String lxcArch;


    //template's md5sum hash
    @Id
    @Expose
    private String md5sum;


    @Id
    @Expose
    private String templateVersion;


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

    @Lob
    //contents of packages manifest file
    private String packagesManifest;

    //    @ManyToOne( optional = true )
    //    @JoinColumns( {
    //            @JoinColumn( name = "parentTemplate" ), @JoinColumn( name = "parentLxcArch" )
    //    } )
    //    private Template parentTemplate;


    //children of template, this property is calculated upon need and is null by default (see REST API for calculation)
    @Expose
    @OneToMany( fetch = FetchType.EAGER, cascade = { CascadeType.ALL }, orphanRemoval = true )
    private List<Template> children;

    //subutai products present only in this template excluding all subutai products present in the whole ancestry
    // lineage above
    @Expose
    @ElementCollection( targetClass = String.class )
    private Set<String> products;


    //indicates whether this template is in use on any of FAIs connected to Subutai
    @Expose
    @ElementCollection( targetClass = String.class )
    private Set<String> faisUsingThisTemplate = new HashSet<>();

    //indicates where template is generated
    @Expose
    private UUID peerId;

    @Expose
    private boolean remote = false;


    public Template()
    {
    }


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

        this.templateName = lxcUtsname;
        this.lxcArch = lxcArch;
        //        this.pk = new TemplatePK( lxcUtsname, lxcArch );
        this.lxcUtsname = lxcUtsname;
        this.subutaiConfigPath = subutaiConfigPath;
        this.subutaiParent = subutaiParent;
        this.subutaiGitBranch = subutaiGitBranch;
        this.subutaiGitUuid = subutaiGitUuid;
        this.packagesManifest = packagesManifest;
        this.parentTemplateName = subutaiParent;
        this.md5sum = md5sum;

        //        if ( pk.getTemplateName().equalsIgnoreCase( parentTemplateName ) )
        if ( this.templateName.equalsIgnoreCase( parentTemplateName ) )
        {
            parentTemplateName = null;
        }
    }


    public Template( final String lxcArch, final String lxcUtsname, final String subutaiConfigPath,
                     final String subutaiParent, final String subutaiGitBranch, final String subutaiGitUuid,
                     final String packagesManifest, final String md5sum, final String templateVersion )
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
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateVersion ), "Missing templateVersion" );

        this.templateName = lxcUtsname;
        this.lxcArch = lxcArch;
        //        this.pk = new TemplatePK( lxcUtsname, lxcArch );
        this.lxcUtsname = lxcUtsname;
        this.subutaiConfigPath = subutaiConfigPath;
        this.subutaiParent = subutaiParent;
        this.subutaiGitBranch = subutaiGitBranch;
        this.subutaiGitUuid = subutaiGitUuid;
        this.parentTemplateName = subutaiParent;
        this.md5sum = md5sum;
        this.templateVersion = templateVersion;
        this.packagesManifest = packagesManifest;


        //        if ( pk.getTemplateName().equalsIgnoreCase( parentTemplateName ) )
        if ( this.templateName.equalsIgnoreCase( parentTemplateName ) )
        {
            parentTemplateName = null;
        }
    }


    public boolean isInUseOnFAIs()
    {
        return faisUsingThisTemplate != null && !faisUsingThisTemplate.isEmpty();
    }


    public void setInUseOnFAI( final String faiHostname, final boolean inUseOnFAI )
    {
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
            this.children = new ArrayList<>();
        }
        //        for ( Template child : children )
        //        {
        //            child.setParentTemplate( this );
        //        }
        this.children.addAll( children );
    }


    public List<Template> getChildren()
    {
        if ( children != null )
        {
            return Collections.unmodifiableList( children );
        }
        else
        {
            return Collections.emptyList();
        }
    }


    public String getMd5sum()
    {
        return md5sum;
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
        return lxcArch;
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
        return templateName;
    }


    public String getFileName()
    {
        return String.format( "%s-subutai-template_%s_%s.deb", templateName, templateVersion, lxcArch ).toLowerCase();
    }


    public String getParentTemplateName()
    {
        return parentTemplateName;
    }


    public String getTemplateVersion()
    {
        return templateVersion;
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
        Template result = new Template( this.lxcArch, this.lxcUtsname, this.subutaiConfigPath, this.subutaiParent,
                this.subutaiGitBranch, this.subutaiGitUuid, this.packagesManifest, this.md5sum, this.templateVersion );
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

        final Template that = ( Template ) o;

        return lxcArch.equals( that.lxcArch ) && md5sum.equals( that.md5sum ) && templateName
                .equals( that.templateName ) && templateVersion.equals( that.templateVersion );
    }


    @Override
    public int hashCode()
    {
        int result = templateName.hashCode();
        result = 31 * result + lxcArch.hashCode();
        result = 31 * result + templateVersion.hashCode();
        result = 31 * result + md5sum.hashCode();
        return result;
    }

    //    @Override
    //    public int hashCode()
    //    {
    //        int result = templateName.hashCode();
    //        result = 31 * result + lxcArch.hashCode();
    //        //        int result = pk.getTemplateName().hashCode();
    //        //        result = 31 * result + pk.getLxcArch().hashCode();
    //        return result;
    //    }
    //
    //
    //    @Override
    //    public boolean equals( final Object o )
    //    {
    //        if ( this == o )
    //        {
    //            return true;
    //        }
    //        if ( o == null || getClass() != o.getClass() )
    //        {
    //            return false;
    //        }
    //
    //        final Template template = ( Template ) o;
    //
    //        return lxcArch.equals( template.getLxcArch() ) && templateName.equals( template.getTemplateName() );
    //        //        return pk.getLxcArch().equals( template.getLxcArch() ) && pk.getTemplateName()
    //        //                                                                    .equals( template.getTemplateName
    // () );
    //    }


    @Override
    public String toString()
    {
        return "Template{" +
                "templateName='" + templateName + '\'' +
                ", parentTemplateName='" + parentTemplateName + '\'' +
                ", lxcArch='" + lxcArch + '\'' +
                ", lxcUtsname='" + lxcUtsname + '\'' +
                ", subutaiConfigPath='" + subutaiConfigPath + '\'' +
                ", subutaiParent='" + subutaiParent + '\'' +
                ", subutaiGitBranch='" + subutaiGitBranch + '\'' +
                ", subutaiGitUuid='" + subutaiGitUuid + '\'' +
                ", children=" + children +
                ", products=" + products +
                ", md5sum='" + md5sum + '\'' +
                ", templateVersion='" + templateVersion + '\'' +
                ", faisUsingThisTemplate=" + faisUsingThisTemplate +
                ", peerId=" + peerId +
                ", remote=" + remote +
                '}';
    }
}
