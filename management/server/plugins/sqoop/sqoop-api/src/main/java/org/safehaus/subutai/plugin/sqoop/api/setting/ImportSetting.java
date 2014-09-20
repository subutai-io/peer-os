package org.safehaus.subutai.plugin.sqoop.api.setting;


import java.util.EnumMap;
import java.util.Map;

import org.safehaus.subutai.plugin.sqoop.api.DataSourceType;


public class ImportSetting extends CommonSetting
{

    DataSourceType type;
    Map<ImportParameter, Object> parameters = new EnumMap( ImportParameter.class );


    public DataSourceType getType()
    {
        return type;
    }


    public void setType( DataSourceType type )
    {
        this.type = type;
    }


    public void addParameter( ImportParameter param, Object value )
    {
        parameters.put( param, value );
    }


    public String getStringParameter( ImportParameter param )
    {
        Object p = getParameter( param );
        return p != null ? p.toString() : null;
    }


    public Object getParameter( ImportParameter param )
    {
        return parameters != null ? parameters.get( param ) : null;
    }


    public boolean getBooleanParameter( ImportParameter param )
    {
        Object p = getParameter( param );
        if ( p == null )
        {
            return false;
        }
        if ( p instanceof Boolean )
        {
            return ( ( Boolean ) p ).booleanValue();
        }
        return Boolean.parseBoolean( p.toString() );
    }


    @Override
    public String toString()
    {
        return super.toString() + ", type=" + type + ", parameters=" + ( parameters != null ? parameters.size() : 0 );
    }
}
