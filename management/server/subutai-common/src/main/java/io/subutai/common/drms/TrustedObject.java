package io.subutai.common.drms;


/**
 * Created by talas on 12/7/15.
 */
//TODO this class should be abstract type, and all descendant child classes will override parent methods so that they
// could be identified in right way
public class TrustedObject
{
    private String classPath;
    private String uniqueIdentifier;


    public TrustedObject( final String classPath, final String uniqueIdentifier )
    {
        this.classPath = classPath;
        this.uniqueIdentifier = uniqueIdentifier;
    }


    public String getClassPath()
    {
        return classPath;
    }


    public void setClassPath( final String classPath )
    {
        this.classPath = classPath;
    }


    public String getUniqueIdentifier()
    {
        return uniqueIdentifier;
    }


    public void setUniqueIdentifier( final String uniqueIdentifier )
    {
        this.uniqueIdentifier = uniqueIdentifier;
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof TrustedObject ) )
        {
            return false;
        }

        final TrustedObject that = ( TrustedObject ) o;

        if ( classPath != null ? !classPath.equals( that.classPath ) : that.classPath != null )
        {
            return false;
        }
        return !( uniqueIdentifier != null ? !uniqueIdentifier.equals( that.uniqueIdentifier ) :
                  that.uniqueIdentifier != null );
    }


    @Override
    public int hashCode()
    {
        int result = classPath != null ? classPath.hashCode() : 0;
        result = 31 * result + ( uniqueIdentifier != null ? uniqueIdentifier.hashCode() : 0 );
        return result;
    }


    @Override
    public String toString()
    {
        return "TrustedObject{" +
                "classPath='" + classPath + '\'' +
                ", uniqueIdentifier='" + uniqueIdentifier + '\'' +
                '}';
    }
}
