package org.safehaus.subutai.core.packge.api;


import java.io.Serializable;
import java.util.Objects;


public class PackageInfo implements Comparable<PackageInfo>, Serializable {

    public static final String SOURCE_NAME = "PackageManager";
    private static final long serialVersionUID = 22L;
    private final String name;
    private final String version;
    private String arch;
    private String description;


    public PackageInfo( String name, String version )
    {
        this.name = name;
        this.version = version;
    }


    public String getName()
    {
        return name;
    }


    public String getVersion()
    {
        return version;
    }


    public String getArch()
    {
        return arch;
    }


    public void setArch( String arch )
    {
        this.arch = arch;
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
    public int hashCode()
    {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode( this.name );
        hash = 29 * hash + Objects.hashCode( this.version );
        return hash;
    }


    @Override
    public boolean equals( Object obj )
    {
        if ( obj instanceof PackageInfo )
        {
            PackageInfo other = ( PackageInfo ) obj;
            return name.equals( other.name ) && version.equals( other.version );
        }
        return false;
    }


    @Override
    public String toString()
    {
        String sep = "\t";
        StringBuilder sb = new StringBuilder();
        sb.append( name ).append( sep ).append( version );
        sb.append( sep ).append( arch ).append( sep ).append( description );
        return sb.toString();
    }


    @Override
    public int compareTo( PackageInfo o )
    {
        int c = name.compareTo( o.name );
        return c != 0 ? c : version.compareTo( o.version );
    }
}
