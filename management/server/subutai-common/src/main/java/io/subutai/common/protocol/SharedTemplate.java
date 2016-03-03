package io.subutai.common.protocol;


public class SharedTemplate extends TemplateKurjun
{

    private String toUserFprint;


    public SharedTemplate( String id, String md5Sum, String name, String version, String architecture, String parent,
            String packageName, String ownerFprint, String toUserFprint )
    {
        super( id, md5Sum, name, version, architecture, parent, packageName, ownerFprint );
        this.toUserFprint = toUserFprint;
    }


    public String getToUserFprint()
    {
        return toUserFprint;
    }


    public void setToUserFprint( String toUserFprint )
    {
        this.toUserFprint = toUserFprint;
    }

}
