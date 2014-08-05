package org.safehaus.subutai.impl.manager.util;


import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.api.manager.helper.EnvironmentBlueprint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;


/**
 * Blueprint Parser Utility class
 */
public class BlueprintParser {

    Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();


    public EnvironmentBlueprint parseEnvironmentBlueprintText( final String blueprintStr ) {
        try {
            return gson.fromJson( blueprintStr, EnvironmentBlueprint.class );
        }
        catch ( JsonSyntaxException e ) {
            System.out.println( "Error parsing blueprint" );
        }
        return null;
    }


    public String parseEnvironmentBlueprint( EnvironmentBlueprint blueprint ) {
        //TODO catch parse exception
        return gson.toJson( blueprint, EnvironmentBlueprint.class );
    }


    public Environment parseEnvironment( final String blueprintStr ) {
        try {
            return gson.fromJson( blueprintStr, Environment.class );
        }
        catch ( JsonSyntaxException e ) {
            System.out.println( "Error parsing blueprint" );
        }
        return null;
    }
}
