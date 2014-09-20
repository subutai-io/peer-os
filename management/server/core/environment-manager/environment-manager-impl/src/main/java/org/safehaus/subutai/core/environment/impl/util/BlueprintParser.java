package org.safehaus.subutai.core.environment.impl.util;


import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.core.environment.api.helper.Environment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;


/**
 * Blueprint Parser Utility class
 */
public class BlueprintParser
{

    Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();


    public EnvironmentBlueprint parseEnvironmentBlueprintText( final String blueprintStr ) throws JsonSyntaxException
    {
        try
        {
            EnvironmentBlueprint eb = gson.fromJson( blueprintStr, EnvironmentBlueprint.class );
            return eb;
        }
        catch ( JsonSyntaxException e )
        {
            throw new JsonSyntaxException( "Error parsing blueprint" );
        }
    }


    public String parseEnvironmentBlueprint( EnvironmentBlueprint blueprint ) throws JsonSyntaxException
    {
        return gson.toJson( blueprint, EnvironmentBlueprint.class );
    }


    public Environment parseEnvironment( final String blueprintStr ) throws JsonSyntaxException
    {
        return gson.fromJson( blueprintStr, Environment.class );
    }
}
