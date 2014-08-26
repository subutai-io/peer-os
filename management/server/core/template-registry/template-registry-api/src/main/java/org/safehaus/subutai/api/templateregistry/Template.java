package org.safehaus.subutai.api.templateregistry;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.annotations.Expose;


/**
 * Template represents template entry in registry
 */
public class Template {

    //name of template
    @Expose
    private String templateName;

    //name of parent template
    @Expose
    private String parentTemplateName;

    //lxc architecture e.g. amd64, i386
    @Expose
    private String lxcArch;

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
    private String packagesManifest;

    //children of template, this property is calculated upon need and is null by default (see REST API for calculation)
    @Expose
    private List<Template> children;

    //subutai products present only in this template excluding all subutai products present in the whole ancestry
    // lineage above
    @Expose
    private Set<String> products;

    //template's md5sum hash
    @Expose
    private String md5sum;


    public Template( final String lxcArch, final String lxcUtsname, final String subutaiConfigPath,
                     final String subutaiParent, final String subutaiGitBranch, final String subutaiGitUuid,
                     final String packagesManifest, final String md5sum ) {
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

        this.lxcArch = lxcArch;
        this.lxcUtsname = lxcUtsname;
        this.subutaiConfigPath = subutaiConfigPath;
        this.subutaiParent = subutaiParent;
        this.subutaiGitBranch = subutaiGitBranch;
        this.subutaiGitUuid = subutaiGitUuid;
        this.packagesManifest = packagesManifest;
        this.templateName = lxcUtsname;
        this.parentTemplateName = subutaiParent;
        this.md5sum = md5sum;

        if ( templateName.equalsIgnoreCase( parentTemplateName ) ) {
            parentTemplateName = null;
        }
    }


    public void addChildren( List<Template> children ) {
        if ( this.children == null ) {
            this.children = new ArrayList<>();
        }
        this.children.addAll( children );
    }


    public String getMd5sum() {
        return md5sum;
    }


    public Set<String> getProducts() {
        return products;
    }


    public void setProducts( final Set<String> products ) {
        this.products = products;
    }


    public String getLxcArch() {
        return lxcArch;
    }


    public String getLxcUtsname() {
        return lxcUtsname;
    }


    public String getSubutaiConfigPath() {
        return subutaiConfigPath;
    }


    public String getSubutaiParent() {
        return subutaiParent;
    }


    public String getSubutaiGitBranch() {
        return subutaiGitBranch;
    }


    public String getSubutaiGitUuid() {
        return subutaiGitUuid;
    }


    public String getPackagesManifest() {
        return packagesManifest;
    }


    public String getTemplateName() {
        return templateName;
    }


    public String getParentTemplateName() {
        return parentTemplateName;
    }


    @Override
    public String toString() {
        return "Template{" +
                "templateName='" + templateName + '\'' +
                ", parentTemplateName='" + parentTemplateName + '\'' +
                ", lxcArch='" + lxcArch + '\'' +
                ", lxcUtsname='" + lxcUtsname + '\'' +
                ", subutaiConfigPath='" + subutaiConfigPath + '\'' +
                ", subutaiParent='" + subutaiParent + '\'' +
                ", subutaiGitBranch='" + subutaiGitBranch + '\'' +
                ", subutaiGitUuid='" + subutaiGitUuid + '\'' +
                ", products=" + products +
                '}';
    }
}
