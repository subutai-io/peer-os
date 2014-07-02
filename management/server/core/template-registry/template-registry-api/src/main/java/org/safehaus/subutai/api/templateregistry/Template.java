package org.safehaus.subutai.api.templateregistry;


import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * Template represents template entry in registry
 */
public class Template {

    public static final String MASTER_TEMPLATE_NAME = "master";
    private static Template masterTemplate = new Template();
    //name of template
    private String templateName;
    //name of parent template
    private String parentTemplateName;
    //lxc architecture e.g. amd64, i386
    private String lxcArch;
    //lxc container name
    private String lxcUtsname;
    //path to cfg files tracked by subutai
    private String subutaiConfigPath;
    //path to app data files tracked by subutai
    private String subutaiAppdataPath;
    //name of parent template
    private String subutaiParent;
    //name of git branch where template cfg files are versioned
    private String subutaiGitBranch;
    //id of git commit which pushed template cfg files to git
    private String subutaiGitUuid;
    //contents of packages manifest file
    private String packagesManifest;

    private List<Template> children;


    public Template( final String lxcArch, final String lxcUtsname, final String subutaiConfigPath,
                     final String subutaiAppdataPath, final String subutaiParent, final String subutaiGitBranch,
                     final String subutaiGitUuid, final String packagesManifest ) {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcUtsname ), "Missing lxc.utsname parameter" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcArch ), "Missing lxc.arch parameter" );
        Preconditions
                .checkArgument( !Strings.isNullOrEmpty( subutaiConfigPath ), "Missing subutai.config.path parameter" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( subutaiAppdataPath ),
                "Missing subutai.app.data.path parameter" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( subutaiParent ), "Missing subutai.parent parameter" );
        Preconditions
                .checkArgument( !Strings.isNullOrEmpty( subutaiGitBranch ), "Missing subutai.git.branch parameter" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( subutaiGitUuid ), "Missing subutai.git.uuid parameter" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( packagesManifest ), "Missing packages manifest" );
        this.lxcArch = lxcArch;
        this.lxcUtsname = lxcUtsname;
        this.subutaiConfigPath = subutaiConfigPath;
        this.subutaiAppdataPath = subutaiAppdataPath;
        this.subutaiParent = subutaiParent;
        this.subutaiGitBranch = subutaiGitBranch;
        this.subutaiGitUuid = subutaiGitUuid;
        this.packagesManifest = packagesManifest;
        this.templateName = lxcUtsname;
        this.parentTemplateName = subutaiParent;
    }


    private Template() {
        templateName = MASTER_TEMPLATE_NAME;
    }


    public static Template getMasterTemplate() {
        return masterTemplate;
    }


    public void addChildren( List<Template> children ) {
        if ( this.children == null ) {
            this.children = new ArrayList<>();
        }
        this.children.addAll( children );
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


    public String getSubutaiAppdataPath() {
        return subutaiAppdataPath;
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
                ", subutaiAppdataPath='" + subutaiAppdataPath + '\'' +
                ", subutaiParent='" + subutaiParent + '\'' +
                ", subutaiGitBranch='" + subutaiGitBranch + '\'' +
                ", subutaiGitUuid='" + subutaiGitUuid + '\'' +
                ", packagesManifest='" + packagesManifest + '\'' +
                '}';
    }
}
