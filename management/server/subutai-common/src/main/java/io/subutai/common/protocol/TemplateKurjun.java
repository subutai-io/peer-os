package io.subutai.common.protocol;


import org.codehaus.jackson.annotate.JsonProperty;


public class TemplateKurjun
{

    @JsonProperty( "id" )
    private String id;

    @JsonProperty( "md5Sum" )
    private String md5Sum;

    @JsonProperty( "name" )
    private String name;

    @JsonProperty( "version" )
    private String version;

    @JsonProperty( "architecture" )
    private String architecture;

    @JsonProperty( "parent" )
    private String parent;

    @JsonProperty( "packageName" )
    private String packageName;

    @JsonProperty( "ownerFprint" )
    private String ownerFprint;

    @JsonProperty( "configContents" )
    private String configContents;

    @JsonProperty( "packagesContents" )
    private String packagesContents;

    @JsonProperty( "size" )
    private long size;


    public TemplateKurjun( String id, String md5Sum, String name, String version, String architecture, String parent,
                           String packageName, String ownerFprint )
    {
        this.id = id;
        this.md5Sum = md5Sum;
        this.name = name;
        this.version = version;
        this.architecture = architecture;
        this.parent = parent;
        this.packageName = packageName;
        this.ownerFprint = ownerFprint;
    }


    public void setSize( final long size )
    {
        this.size = size;
    }


    public long getSize()
    {
        return size;
    }


    public String getId()
    {
        return id;
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


    public String getOwnerFprint()
    {
        return ownerFprint;
    }


    public void setOwnerFprint( String ownerFprint )
    {
        this.ownerFprint = ownerFprint;
    }


    @Override
    public String toString()
    {
        return "TemplateKurjun{" + "md5Sum=" + md5Sum + ", name=" + name + ", version=" + version + ", architecture="
                + architecture + ", parent=" + parent + ", packageName=" + packageName + ", ownerFprint=" + ownerFprint
                + '}';
    }
}
