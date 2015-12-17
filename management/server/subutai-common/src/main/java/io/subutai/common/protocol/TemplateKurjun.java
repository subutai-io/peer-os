package io.subutai.common.protocol;


import com.google.gson.annotations.Expose;


public class TemplateKurjun
{

    @Expose
    private String md5Sum;
    @Expose
    private String name;
    @Expose
    private String version;
    @Expose
    private String architecture;
    @Expose
    private String parent;

    private String packageName;

    @Expose
    private String configContents;
    @Expose
    private String packagesContents;


    public TemplateKurjun( String md5Sum, String name, String version, String architecture, String parent, String packageName )
    {
        this.md5Sum = md5Sum;
        this.name = name;
        this.version = version;
        this.architecture = architecture;
        this.parent = parent;
        this.packageName = packageName;
    }


    public String getMd5Sum()
    {
        return md5Sum;
    }


    public void setMd5Sum( String md5Sum )
    {
        this.md5Sum = md5Sum;
    }


    public String getName()
    {
        return name;
    }


    public void setName( String name )
    {
        this.name = name;
    }


    public String getVersion()
    {
        return version;
    }


    public void setVersion( String version )
    {
        this.version = version;
    }


    public String getArchitecture()
    {
        return architecture;
    }


    public void setArchitecture( String architecture )
    {
        this.architecture = architecture;
    }


    public String getParent()
    {
        return parent;
    }


    public void setParent( String parent )
    {
        this.parent = parent;
    }


    public String getPackageName()
    {
        return packageName;
    }


    public void setPackageName( String packageName )
    {
        this.packageName = packageName;
    }


    public String getConfigContents()
    {
        return configContents;
    }


    public void setConfigContents( String configContents )
    {
        this.configContents = configContents;
    }


    public String getPackagesContents()
    {
        return packagesContents;
    }


    public void setPackagesContents( String packagesContents )
    {
        this.packagesContents = packagesContents;
    }


    @Override
    public String toString()
    {
        return "TemplateKurjun{" + "md5Sum=" + md5Sum + ", name=" + name + ", version=" + version + ", architecture=" + architecture + ", parent=" + parent + ", packageName=" + packageName + '}';
    }

}
