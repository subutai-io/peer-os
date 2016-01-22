package io.subutai.common.protocol;


import com.google.gson.annotations.Expose;


public class AptPackage
{
    @Expose
    private String md5Sum;
    @Expose
    private String name;
    @Expose
    private String version;
    @Expose
    private String source;
    @Expose
    private String maintainer;
    @Expose
    private String architecture;
    @Expose
    private int installedSize;
    @Expose
    private String description;


    public AptPackage( String md5Sum, String name, String version, String source, String maintainer, String architecture, int installedSize, String description )
    {
        this.md5Sum = md5Sum;
        this.name = name;
        this.version = version;
        this.source = source;
        this.maintainer = maintainer;
        this.architecture = architecture;
        this.installedSize = installedSize;
        this.description = description;
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


    public String getSource()
    {
        return source;
    }


    public void setSource( String source )
    {
        this.source = source;
    }


    public String getMaintainer()
    {
        return maintainer;
    }


    public void setMaintainer( String maintainer )
    {
        this.maintainer = maintainer;
    }


    public String getArchitecture()
    {
        return architecture;
    }


    public void setArchitecture( String architecture )
    {
        this.architecture = architecture;
    }


    public int getInstalledSize()
    {
        return installedSize;
    }


    public void setInstalledSize( int installedSize )
    {
        this.installedSize = installedSize;
    }


    public String getDescription()
    {
        return description;
    }


    public void setDescription( String description )
    {
        this.description = description;
    }


    @Override
    public String toString()
    {
        return "AptPackage{" + "md5Sum=" + md5Sum + ", name=" + name + ", version=" + version + ", source=" + source + ", architecture=" + architecture + ", installedSize=" + installedSize + '}';
    }
    
}
