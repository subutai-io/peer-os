package org.safehaus.subutai.common.util;


/**
 * Created by nisakov on 3/5/15.
 */
public class UrlUtil
{
    public static String getQueryParameterValue(String paramName,String query)
    {
        String paramValue = "";

        if(query != null)
        {
            String parameters[] = query.split( "&" );

            if ( parameters != null )
            {
                for ( int x = 0; x < parameters.length; x++ )
                {
                    String subParameters[] = parameters[x].split( "=" );

                    if ( subParameters[0].equals( paramName ) )
                    {
                        paramValue = subParameters[1];
                        break;
                    }
                }
            }
        }

        return paramValue;
    }
}
