package org.safehaus.subutai.core.environment.impl.entity;


import java.io.Serializable;


/**
 * Created by nisakov on 1/12/15.
 */

//*************** Primary Key ************************************
class EnvironmentBuildProcessEntityPk implements Serializable
{
    private String id;
    private String source;

    public EnvironmentBuildProcessEntityPk()
    {

    }
    public String getId()
    {
        return id;
    }


    public void setId( final String id )
    {
        this.id = id;
    }


    public String getSource()
    {
        return source;
    }


    public void setSource( final String source )
    {
        this.source = source;
    }
    @Override
    public boolean equals(Object obj)
    {
        if(obj instanceof EnvironmentBuildProcessEntityPk){
            EnvironmentBuildProcessEntityPk envBPPK = (EnvironmentBuildProcessEntityPk) obj;

            if(!envBPPK.getId().equals( this.id )){
                return false;
            }

            if(!envBPPK.getSource().equals( this.source )){
                return false;
            }

            return true;
        }

        return false;
    }
    @Override
    public int hashCode()
    {
        return id.hashCode() + source.hashCode();
    }

}
